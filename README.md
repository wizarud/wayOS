## wayOS

Yum! Yum! Yum! ..(^o^)ๆ=>

## Motivation

The basic idea behind this project is “Make anything speak!” So I started develop core engine with simple array of predefined pairs of keywords and answer text. When user enter the input, The engine has to find out what is the best fit? of input and all those keywords.

Best fit algorithm can be done by scoring the intersection matched between input and each keyword. If same score occur for difference answers, pick one randomly. Thanks for ATH who recommend this :)

But speak is never enough when I decided to add more magic with embedded expressions. So this chatbot can do some logical tasks such as create and update variables, forwarding to the next keywords with or without parameters to make the conditional decisions, waiting for the answer as a question and can also call the RESTful webservices. You can develop your chatbot application with logic designer tool base on style of codeless.

First version before this can only worked in LINE, Facebook Page Messenger or Web via http channels, But wayOS can also run in any java runtime supported devices. 

You can setup your chatbot application as microservices or chatbot as a service or whatever called. And if you have a little bit of java development skills, You can develop your own connectors for another channels of your own platforms. Integrate such an IoT device as a input/output channel to work together with another one. wayOS wraps them all and help us by provided the simple tools and API that easily access by chatbot, all of services are available in your hand! (^o^)ๆ

## Build status
To be updated later

## Screenshots

### Logic Designer
<img class="CSS_LIGHTBOX_SCALED_IMAGE_IMG" src="https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEg4UufXrW09wtCWnf0ne8UTKkUTCGWOCx29fIMID4syiC9SBPTLfrMGeOEHC3oo6HjnpE8MO473rk6OXX8ve8351hpZt7g1g0KBYIR4myzqX8Nx__bmJFoR9XFIOYPVWsL5rjzEopZWmoYWKoqaTkmbsQn1SlhOX5q8UfYgkFRKCW6Qa0bJcCityA5Ygw/s1391/dirable%20wayobot.png" style="width: 580px; height: 345px;">

### Stock Indicator

<img class="CSS_LIGHTBOX_SCALED_IMAGE_IMG" src="https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEj7CC7ttCIaRjccYpvjBo1wPo4kHebWV7BIfW1q2Y1N8sk7NuHCx9av76QJ9fYl6vw1SetWGP6WnEdid4yKUghzdK3nmFxBUheYHGrvtp7qofAXowVnvgiriTxNkoEXtrb4EUjQfWw_4VXEFVe5fPT8vnvK5cTazUuhFOFeoW8qt3qTfwr5dATWa0eVGQ/s2436/yod%20stock%20wayobot.png" style="width: 159px; height: 345px;">

### Shopping Cart

<img class="CSS_LIGHTBOX_SCALED_IMAGE_IMG" src="https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEi9ISuLDh3meuCk27pB8-jhUYs8vQWKPKHKRR8haxCbXKlK-IZMI5bSra9D3etUHc03R6kCcEdLGnVk2KG5DoMCHCRSP7YRGfMJMAZW6I3oKL1CAiqWuFmhvykRyfJEhbdBzqOqzmuvTuU0II3S6Sg3rQZwSFZ9LR6fErjvMmKDn-VSRsBOObBGS38EcQ/s2436/appchain.PNG" style="width: 159px; height: 345px;">

<img class="CSS_LIGHTBOX_SCALED_IMAGE_IMG" src="https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEj0rMyAB0mpJ-l9gy6HzsCHAFI1ogvMZdRGJlPHPi-0ag1wCuUwJexlhcx9sf3HwKT72QzTeE7PYK5Xrw2AwYidizzK-BuHeja2f1TdboBwC3a_NS190-NeTepbiSLxItgGxlO8j5vbkUANFob4kvtTvFUSOT4v9T-evNIH1eN0YkRZiPrNpEclAcATrA/s2436/POS.PNG" style="width: 159px; height: 345px;">

## Technology Stack

### Stand Alone Application

<img class="CSS_LIGHTBOX_SCALED_IMAGE_IMG" src="https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEgDqJiewuZaXdOBW7VnfxoWCi7oTRpyr8mytbiJO8qltyxMcbuTYdpUIhwSQq8kfEN1NgrRYlHehcp7VhRyFuY0ukqgiFYvgsmnJaJRKDEvAKQYTCRV1W1IuJ7-7jmsAw6qlWspX7y9DgmrjBQQstnNQCHc0iVLtO520xLLmqo2hZw-F55shKUhImBv2g/s425/wayOS%20Stand%20Alone%20Stack.png" style="width: 268px; height: 345px;">

### Google AppEngine with Built-in Channels such as LINE/Facebook

<img class="CSS_LIGHTBOX_SCALED_IMAGE_IMG" src="https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEjVxQlACm5Ur3gNG6OTiUO680o700db1JZgx7gX542hSho8A95Ig5JVy64yfmfVHqiK59I8gOSH7zYu6CxVPTalR0rOM46qUg3YouGFRun839IQhUn8itT7dabli-9i5nBjrD381ycw3n95MzWbK5rKJZWqJcVGf367bLYwzhIf1zRoCpurq38u1Khc8A/s1010/wayOS%20Platform.png" style="width: 643px; height: 345px;">


## Features ..(^o^)ๆ

- Automatic generate UI from your logic flows such as question, slide and quick menus so you don’t need to worry about the screen design just focus on your business process.

- All variables are persistent that would help you to keep status of the user’s information such as name, address, order including appointment status. You can do such a simple checking logic to prevent them from refilling again.

- There are many tools that help you to build your own chatbot application such as catalog spreadsheet or from built-in templates. So you can customize by simply change some properties to match your business informations.

- Users can access your chatbot via many channels such as LINE/Facebook/Http (REST API) or even from another chatbot. You can push a logic that directly notify to some user, all users or only administrator who got the authorization. This process can be done by single step of flow that manipulate value of the Action Variable.

## Installation
To be updated later

## How to use?
To be updated later

## Contributor

**Wisarut Srisawet**

Special thanks for M&D, My friends and family to help this project happens!

## License
A free and open source project.
MIT © [eoss-th]()