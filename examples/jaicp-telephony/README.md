# Telephony bot example

This example shows how it's easy to create chatbot that integrates with JAICP and process incoming telephony call requests using speech recognition and synthesis.

## How to use

1. Create a new JAICF project in [JAICP Application Panel](https://app.jaicp.com).
2. Add a [SIP Server connection and new telephony channel](https://help.just-ai.com/#/docs/en/telephony/telephony_setup).
3. Copy your project's API key from _Project Properties_ -> _Location_.
4. Paste it to `jaicp.properties`.

Then you can run `JicpPoller.kt` file and test your project making calls to the phone number you've configured on step 2.

### About SIP

SIP trunk is telephone line over IP, which is controlled by SIP (Session Initiation Protocol).
There are many providers, using which you can create a free SIP trunk and test your bot.

For example, you can use [Zadarma](https://zadarma.com/en/) to get a virtual number.
