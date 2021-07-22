rootProject.name = "jaicf"

include("core")

include("examples:hello-world")
findProject(":examples:hello-world")?.name = "hello-world"
include("examples:jaicp-telephony")
findProject(":examples:jaicp-telephony")?.name = "jaicp-telephony"
include("examples:game-clock")
findProject(":examples:game-clock")?.name = "game-clock"
include("examples:viber-example")
findProject(":examples:viber-example")?.name = "viber-example"
include("examples:multilingual-bot")
findProject(":examples:multilingual-bot")?.name = "multilingual-bot"

include("activators:dialogflow")
findProject(":activators:dialogflow")?.name = "dialogflow"
include("activators:caila")
findProject(":activators:caila")?.name = "caila"
include("activators:lex")
findProject(":activators:lex")?.name = "lex"

include("channels:alexa")
findProject(":channels:alexa")?.name = "alexa"
include("channels:slack")
findProject(":channels:slack")?.name = "slack"
include("channels:telegram")
findProject(":channels:telegram")?.name = "telegram"
include("channels:viber")
findProject(":channels:viber")?.name = "viber"
include("channels:aimybox")
findProject(":channels:aimybox")?.name = "aimybox"
include("channels:facebook")
findProject(":channels:facebook")?.name = "facebook"
include("channels:yandex-alice")
findProject(":channels:yandex-alice")?.name = "yandex-alice"
include("channels:google-actions")
findProject(":channels:google-actions")?.name = "google-actions"
include("channels:jaicp")
findProject(":channels:jaicp")?.name = "jaicp"

include("managers:mongo")
findProject(":managers:mongo")?.name = "mongo"
include("managers:mapdb")
findProject(":managers:mapdb")?.name = "mapdb"
include("activators:rasa")
findProject(":activators:rasa")?.name = "rasa"
