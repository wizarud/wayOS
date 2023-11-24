## wayOS Runtime
Runtime for wayOS Web (Chatbot Designer)

## Motivation
Programmable Chatbot

## Build status
Ready for customization.

## Technology Stack
- Context file in JSON format that contains nodes array of entities. A Entity has hooks array of keywords and a response message.

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
- Http Endpoints for REST Api including LINE and Facebook.
- Programmatic Features such as forwarding, built-in and session variables, conditional and also support REST calling, WebScrapping (Jsoup).
- Additional Features such as Context Builder by using TSV or just the simple text for FAQ, Form, Quiz and Catalog that has the add to cart menu.

## Installation
- Install as jar to the WEB-INF/lib of [Play](https://github.com/wizarud/play) Web Project.

## Contributor

**Wisarut Srisawet**

## License
A free and open source project.
MIT © [wayOS](https://wayos.yiem.ai)