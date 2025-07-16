package com.justai.jaicf.examples.llm

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.activator.llm.tool.http.httpGet
import com.justai.jaicf.activator.llm.tool.llmTool
import com.justai.jaicf.channel.ConsoleChannel


/**
 * This example shows how an agent can use HTTP calls as a tool.
 * There are ready-to-use one-liners `httpGet`, `httpPost`, `httpDelete`, `httpPut`, `httpPatch`, `httpForm` return an `LLMToolFunction` implementation that make HTTP call to the specified URL via Ktor client.
 * Ktor request and response builders can be provided if needed.
 * By default, all these one-liners propagate tool call arguments to the corresponding request URL params (for GET and DELETE) or JSON body (for POST, PUT, PATCH).
 * Placeholders `{}` in URL string can be used to propagate any tool call arguments as well.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */

@JsonClassDescription("List all recipes in database")
private class ListRecipes

// Tool to fetch recipes from HTTP service
private val ListRecipesTool = llmTool<ListRecipes>(
    httpGet("https://dummyjson.com/recipes?limit=50&select=id,name,prepTimeMinutes,cookTimeMinutes,difficulty,caloriesPerServing")
)

@JsonClassDescription("Get recipe details")
private class GetRecipe(val id: Int)

// Tool to get particular recipe details from HTTP service
private val GetRecipeTool = llmTool<GetRecipe>(
    httpGet("https://dummyjson.com/recipes/{id}")  // Placeholders can be used to fill URL parts with tool argument fields
)

private val AddToCartTool = llmTool<GetRecipe>(
    name = "AddToCart",
    description = "Add recipe ingredients to cart"
) {
    "Ingredients of recipe [${call.arguments.id}] added to cart"
}

private val agent = LLMAgent(
    name = "recipes",
    model = "gpt-4.1-mini",
    instructions = "Assist user with recipes from your database. Reply with markdown and write a lot of emojis.",
    tools = listOf(
        ListRecipesTool,
        GetRecipeTool,
        AddToCartTool.withConfirmation(),
    )
) {
    // Custom action block just for tool calling progress output
    activator.withToolCalls {
        activator.contentStream().forEach(::print)
        if (activator.hasToolCalls()) {
            activator.toolCalls()
                .joinToString(", ", "CALLING ", "...") {
                    it.function().name()
                }.also(::println)
        }
    }
}

fun main() {
    ConsoleChannel(agent.asBot).run("List all recipes you have")
}