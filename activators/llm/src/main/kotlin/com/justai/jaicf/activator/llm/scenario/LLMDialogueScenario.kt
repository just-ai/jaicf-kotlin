//package com.justai.jaicf.activator.llm.scenario
//
//import com.fasterxml.jackson.databind.JsonNode
//import com.fasterxml.jackson.databind.node.ObjectNode
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import com.justai.jaicf.activator.llm.*
//import com.justai.jaicf.activator.llm.client.LLMRequest
//import com.justai.jaicf.builder.ScenarioDsl
//import com.justai.jaicf.builder.ScenarioGraphBuilder
//import com.justai.jaicf.context.ActionContext
//import com.justai.jaicf.plugin.StateDeclaration
//
//typealias LLMMessageActionContext = ActionContext<LLMActivatorContext, *, *>
//typealias LLMPromptGenerator = LLMMessageActionContext.() -> String
//
//private val mapper = jacksonObjectMapper()
//
//private fun LLMMessageActionContext.mergeModel(
//    modelKey: String,
//    model: ObjectNode
//): ObjectNode {
//    val current = context.session[modelKey] as? ObjectNode ?: mapper.createObjectNode()
//    model.fieldNames().forEach { field ->
//        if (!model[field].isNull && (!model[field].isTextual || model[field].asText() != "null")) {
//            current.set<JsonNode>(field, model.get(field))
//        }
//    }
//    context.session[modelKey] = current
//    return current
//}
//
//@ScenarioDsl
//@StateDeclaration
//fun ScenarioGraphBuilder<*, *>.llmDialogue(
//    dialogueId: String,
//    builder: LLMDialogueBuilder.() -> Unit) {
//
//    val modelKey = "llm_dialogue_model_${dialogueId}"
//    val dialogue = LLMDialogueBuilder().apply(builder)
//    val modelPrompt = "Extract fields from user request. Your response must be a JSON with next optional fields: \n" +
//        dialogue.fields.joinToString("\n") { " - ${it.name}: ${it.description} (null by default)" } +
//        "\n\nSet null value for fields that cannot be extracted from user input. Return only these fields with actual values or null by default."
//
//    state(dialogueId) {
//        action {
//            context.session[modelKey] = dialogue.model(this)
//            context.llmSettings = context.llmSettings.copy(
//                responseFormat = LLMRequest.ResponseFormat.json
//            )
//            context.llmChatHistory = context.llmChatHistory
//                .filter { !it.role.isSystem } +
//                LLMRequest.Message.system(dialogue.modelPrompt(this) + "\n\n" + modelPrompt)
//        }
//
//        state("step") {
//            activators {
//                llmActivator()
//            }
//
//            action(llmMessage) {
//                val model = mergeModel(modelKey, activator.fromJson<ObjectNode>())
//                println(model.toPrettyString())
//                val field = dialogue.fields.find { f -> !model.has(f.name) && f.promptAction != null }
//                if (field == null) {
//                    reactions.go("../finish")
//                } else {
//                    val systemPrompt = dialogue.systemPrompt(this)
//                    val fieldPrompt = field.promptAction!!()
//                    val stepPrompt = dialogue.stepPrompt(this, fieldPrompt)
//                    context.llmSettings = context.llmSettings.copy(
//                        responseFormat = when {
//                            dialogue.response.isEmpty() -> LLMRequest.ResponseFormat.text
//                            else -> LLMRequest.ResponseFormat.json
//                        }
//                    )
//                    activator.activateWithSystemMessage(
//                        "${systemPrompt}\n\n" +
//                            dialogue.response.takeIf { it.isNotEmpty() }?.let { fields ->
//                                "Your response must be a JSON object with fields:\n" +
//                                    fields.joinToString("\n") { " - ${it.first}: ${it.second}" }
//                            }.orEmpty() + "\n\n" + stepPrompt
//                    )
//                }
//            }
//
//            state("reply") {
//                activators {
//                    llmActivator()
//                }
//
//                action(llmMessage) {
//                    val json = when {
//                        context.llmSettings.isJsonFormat -> activator.fromJson<ObjectNode>()
//                        else -> mapper.createObjectNode()
//                    }
//                    val model = mergeModel(modelKey, json)
//                    context.llmSettings = context.llmSettings.copy(
//                        responseFormat = LLMRequest.ResponseFormat.json
//                    )
//                    activator.setSystemMessage(
//                        dialogue.modelPrompt(this) + "\n\n" + modelPrompt
//                    )
//                    reactions.changeState("../../")
//                    dialogue.replyCallback(this, model)
//                }
//            }
//        }
//
//        state("finish") {
//            action(llmMessage) {
//                val model = context.session[modelKey] as ObjectNode
//                dialogue.finishCallback(this, model)
//            }
//        }
//    }
//}
//
//class LLMDialogueBuilder {
//    internal val fields = mutableListOf<Field>()
//    internal var finishCallback: LLMMessageActionContext.(ObjectNode) -> Unit = {}
//    internal var replyCallback: LLMMessageActionContext.(ObjectNode) -> Unit = {
//        reactions.say(activator.content)
//    }
//
//    var model: ActionContext<*, *, *>.() -> ObjectNode = { mapper.createObjectNode() }
//    var systemPrompt: LLMMessageActionContext.() -> String = {""}
//    var modelPrompt: ActionContext<*, *, *>.() -> String = {""}
//    var stepPrompt: LLMMessageActionContext.(String) -> String = { "Answer accordingly and then $it" }
//    var response = emptyList<Pair<String, String>>()
//
//    fun field(
//        name: String,
//        description: String,
//        prompt: LLMPromptGenerator? = null) {
//
//        fields.add(Field(name, description, prompt))
//    }
//
//    fun reply(callback: LLMMessageActionContext.(ObjectNode) -> Unit) {
//        replyCallback = callback
//    }
//
//    @ScenarioDsl
//    @StateDeclaration
//    fun finish(callback: LLMMessageActionContext.(ObjectNode) -> Unit) {
//        finishCallback = callback
//    }
//
//    internal data class Field(
//        val name: String,
//        val description: String,
//        val promptAction: LLMPromptGenerator?
//    )
//}