## wayOS Runtime
Runtime for [Play](https://github.com/wizarud/Play), Chatbot designer and player on web.

## Motivation
Programmable Chatbot that support variable, condition, loop and calling WebServices API.

## Technology Stack
- Context file in JSON format that contains nodes array of entities. A Entity has hooks array of keywords and a response message. You can use [Play](https://github.com/wizarud/Play) to create the context.

```

{
  "nodes": [
    {
      "hooks": [
        {
          "text": "hi",
          "match": "Words",
          "weight": 1
        }
      ],
      "response": "Hello World"
    }
  }
]
...
}
```

- The following folders in storage path are use by wayOS such as libs/ to contains context files, private/ for channel credentials (LINE, Facebook), users/ for authentication credentials, vars/ for session variables, public/ for user resource files such as image, audio and video content and logs/ for logging variables.

```
libs/
logs/
private/
public/
users/
vars/
```

- Java 1.8

## Features
- Support Context file that contains the array of key-value pairs for keyword and response messages.
- Interpret Context by finding the best fit of keyword and response that match the user's message.
- Generate Text, Image, Menu for HTML, LINE and Facebook Page Messenger in JSON Format.
- Webhooks endpoints for LINE, Facebook and simple REST provider.
- Programmatic Features such as forwarding, built-in and session variables, conditional and also support REST calling, WebScrapping (Jsoup).
- Push Notification.
- Additional Features such as Context Builder by using TSV or just the simple text for FAQ, Form, Quiz and Catalog that has the add to cart menu.

## Installation
- Install as jar to the WEB-INF/lib of [Play](https://github.com/wizarud/Play).

## Developer

**Wisarut Srisawet**

## License
A free and open source project.
MIT © [wayOS](https://wayos.yiem.ai)