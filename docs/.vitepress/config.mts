import { defineConfig } from 'vitepress'

export default defineConfig({
  title: "JAICF Documentation",
  description: "Develop conversational chatbots with JAICF from Just AI",
  base: "/",
  
  head: [
    ['link', { rel: 'icon', href: '/favicon.ico' }],
    ['meta', { property: 'og:image', content: 'https://help.jaicf.com/assets/images/jaicf-banner.png' }],
    ['meta', { property: 'og:title', content: 'JAICF Documentation' }],
    ['meta', { property: 'og:description', content: 'Develop conversational chatbots with JAICF from Just AI' }]
  ],

  themeConfig: {
    logo: '/assets/images/header.png',
    
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Quick Start', link: '/pages/Quick-Start' },
      { text: 'GitHub', link: 'https://github.com/just-ai/jaicf-kotlin' }
    ],

    sidebar: [
      {
        text: 'Getting Started',
        items: [
          { text: 'Introduction', link: '/pages/Introduction' },
          { text: 'Installing', link: '/pages/Installing' },
          { text: 'Quick Start', link: '/pages/Quick-Start' },
          { text: 'Quick Start with Dialogflow', link: '/pages/Quick-Start-with-Dialogflow' },
          { text: 'FAQ', link: '/pages/FAQ' }
        ]
      },
      {
        text: 'Scenario DSL',
        collapsed: false,
        items: [
          { text: 'Overview', link: '/pages/dsl/index' },
          { text: 'State', link: '/pages/dsl/state' },
          { text: 'Action', link: '/pages/dsl/action' },
          { text: 'Activator', link: '/pages/dsl/activator' },
          {
            text: 'Activators',
            collapsed: true,
            items: [
              { text: 'Overview', link: '/pages/dsl/activators/index' },
              { text: 'Intent', link: '/pages/dsl/activators/intent' },
              { text: 'Event', link: '/pages/dsl/activators/event' },
              { text: 'Regex', link: '/pages/dsl/activators/regex' },
              { text: 'Any Intent', link: '/pages/dsl/activators/anyIntent' },
              { text: 'Any Event', link: '/pages/dsl/activators/anyEvent' },
              { text: 'Catch All', link: '/pages/dsl/activators/catchAll' }
            ]
          },
          { text: 'Context', link: '/pages/dsl/context' },
          { text: 'Request', link: '/pages/dsl/request' },
          { text: 'Reactions', link: '/pages/dsl/reactions' },
          { text: 'Fallback', link: '/pages/dsl/fallback' },
          { text: 'Only If', link: '/pages/dsl/onlyIf' },
          { text: 'Append', link: '/pages/dsl/append' }
        ]
      },
      {
        text: 'Channels',
        collapsed: false,
        items: [
          { text: 'Overview', link: '/pages/channels/index' },
          { text: 'JAICP', link: '/pages/channels/jaicp' },
          { text: 'Chat Widget', link: '/pages/channels/chat-widget' },
          { text: 'Chat API', link: '/pages/channels/chat-api' },
          { text: 'Telephony', link: '/pages/channels/telephony' },
          { text: 'Aimybox', link: '/pages/channels/aimybox' },
          { text: 'Amazon Alexa', link: '/pages/channels/alexa' },
          { text: 'Google Actions', link: '/pages/channels/google-actions' },
          { text: 'Facebook Messenger', link: '/pages/channels/facebook' },
          { text: 'Slack', link: '/pages/channels/slack' },
          { text: 'Telegram', link: '/pages/channels/telegram' },
          { text: 'Viber', link: '/pages/channels/viber' },
          { text: 'Yandex Alice', link: '/pages/channels/yandex-alice' }
        ]
      },
      {
        text: 'NLU',
        collapsed: false,
        items: [
          { text: 'Overview', link: '/pages/nlu/index' },
          { text: 'Caila', link: '/pages/nlu/Caila' },
          { text: 'Dialogflow', link: '/pages/nlu/Dialogflow' },
          { text: 'Amazon Lex', link: '/pages/nlu/Lex' },
          { text: 'Rasa', link: '/pages/nlu/Rasa' }
        ]
      },
      {
        text: 'Environment',
        collapsed: false,
        items: [
          { text: 'Overview', link: '/pages/env/index' },
          { text: 'Ktor', link: '/pages/env/Ktor' },
          { text: 'Spring Boot', link: '/pages/env/Spring-Boot' },
          { text: 'AWS Lambda', link: '/pages/env/AWS-Lambda' },
          { text: 'Heroku', link: '/pages/env/Heroku' },
          { text: 'Docker', link: '/pages/env/Docker' },
          { text: 'JAICP Cloud', link: '/pages/env/JAICP-Cloud' },
          { text: 'Android', link: '/pages/env/Android' },
          { text: 'MapDB', link: '/pages/env/MapDB' },
          { text: 'MongoDB', link: '/pages/env/MongoDB' }
        ]
      },
      {
        text: 'Features',
        collapsed: false,
        items: [
          { text: 'Overview', link: '/pages/features/index' },
          { text: 'Bot Routing', link: '/pages/features/Bot-Routing' },
          { text: 'BotContext Delegates', link: '/pages/features/BotContext-Delegates' },
          { text: 'Conversation Logging', link: '/pages/features/Conversation-Logging' },
          { text: 'Hooks', link: '/pages/features/Hooks' },
          { text: 'Testing', link: '/pages/features/Testing' }
        ]
      },
      {
        text: 'Deployment',
        items: [
          { text: 'Deployment Guide', link: '/pages/Deployment' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/just-ai/jaicf-kotlin' },
      { icon: 'twitter', link: 'https://twitter.com/JustAIglobal' }
    ],

    footer: {
      message: 'Documentation for JAICF - Just AI Conversational Framework',
      copyright: 'Copyright © Just AI'
    },

    search: {
      provider: 'local'
    }
  }
})
