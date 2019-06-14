# Testing… is not that hard

From time to time we need to talk about this topic. Most of us are familiar with it and we feel comfortable with writing tests, but the truth is that doing so is not a particularly popular topic.

In the past years, the tendency has been using some variant of cool techniques like TDD and BDD, and they are well applied; but most developers don't follow the rules or feel like the deadlines don't allow them to just write unit or integration tests. Geez… I even have heard some people say: "[sic] writing test is for QA teams right?" 

Well… this is my attempt to show that testing is not _that_ hard and can be an easy part of your daily workflow.

Notes: 

* I will refer to "function" when referring to methods or functions indistinctively, that kind of discussion for "the right name" is not relevant for this topic
* Most of my work is on android, so this will probably be examples only in Kotlin/Java, but most of the concepts apply to other languages as well, like `private` in JavaScript can be functions not exposed in the `module.exports` 

## Why you should test

We, as developers, love writing a lot of code. We love making our lives miserably complicated using a lot of different things: 

- "I'm going to use X algorithm to sort these elements"
- "We should add a hashcode function for it"
- "That design pattern will solve all of our problems"
- "Just use this tool for solving that small problem"

And from time to time we manage to get away with our solutions without problems. But time consumes everything, including code, and if you are on the need of using that code you wrote once for that small project and aren't sure if it will work… what will you do?

Testing can help us in a lot of ways. From the perspective of TDD fans we can say that: "testing helps us to not break things", which means that once we have tests, incoming new features/changes won't break the current code and from the TDD's perspective that's true. But not everyone follows TDD on a daily basis so we need a different perspective. **For the everyday developer, testing is a way to "check if that thing just works"**. This means that we can validate that even, when new things arrive to the code (features or changes) the code is still usable and won't generate something unexpected or something weird that is not supposed to happen.

This is the main reason for tests. Testing is the way to go when we want to validate our work. I remember when I was at primary school, a professor showed us how to validate a result from different math operations and in that way we could check our results before giving her our homework. Testing is something similar. Testing is a way to do a check on our code, to validate that everything works as expected. Testing is like tasting the food you are cooking: you take a spoon, give it a little taste and if the flavor is not of your pleasing, you add and mix new things in the food until it gives you the result (taste) you want. You don't fix the spoon or you mouth to make the food taste better or as you want, you go and fix the food and try again with the spoon and with your mouth. Testing is the same, you fix your code, not the tests to make the code work as expected.

> Note: Please taste the food you cook before putting it on plates for your guests. They'll appreciate it.

## When we should test

While there's a common debate on what percentage of "test coverage" (or in human words "how much of the code has tests") you should get. My answer for this is: **the most you can**. You should try to write tests for most of the functionality of your code. There are still things you should keep in mind while trying to achieve good tests. For example, in my first time with tests I asked myself: "_How can I be sure the test is well done or it will run as I intend to?_" and my first thought was: "writing a test!", but obviously that's not the answer. 

Writing tests for things you already have for taken for granted is bad idea. One example of this is writing a test for *GSON* (a famous java library from Google to parse JSON) and checking if it successfully parses a JSON. This is a bad idea because Google writes this library, obviously they already wrote some tests for the main functionality of the library so you are just messing with your time writing test for a well established library. Instead, what you probably want is to write a test for some class/function that uses *GSON* and that's a totally different case and is a valid one.

I ask myself these questions when I want to write a test:

> Do I have control over this code and can change it freely?
>
> Is this code that others can see and use externally?

If the answer is yes to both questions, then you probably need some tests for this fragment of code. 

## When to stop testing

There are cases that shouldn't be part of a test. One example in OOP is having a class that only represents data, i.e. in _Java land_ it will be a POJO (Plain Old Java Object) and in _Kotlin land_ it will be a data class, which has getters and setters. Writing a test for a getter or a setter is useless and **should not** be a case for a test. Tests **should not** be about the data itself but about the "what would happen with this data", and getters/setters are straightforwardly about what happens with the data.

Other cases that apply for *no-tests-required* are: ***private functions***. This happens to be an actual discussion also in sites like StackOverflow. My personal opinion is that, if you have a very complex **_private function_** and you feel like you need tests for it, you probably need to split that function into smaller **_private functions_** and then check if they are really in need of being private or if they are in need of tests at all. This happens a lot in the *OOP* world and even in the *scripting* world, where developers face the needs of testing but they start getting complicated about structuring their tests. There are ways (like reflection in Java or `@VisibleForTesting` in android) to make things that are supposed to be private visible on tests. Still… I suggest avoiding this kind of testing, which involves tricks and cheats to obtain something supposedly not obtainable by simpler ways. 

## How you should test

Almost all languages include some way to enable test mode or they have tools created by other developers to test code. Even the worst thing I've used in my life (BrightScript for Roku) had a way to test over your code.

There's still things you should keep in mind while doing tests:

* **Have a contained environment**. I don't want to talk about complex things (like containers or virtual environments like in python). What I mean by "*a contained environment*" is having a way to avoid your code communicating with sources outside of it. Like real databases or real web services. There are still parts of testing that involve communicating with these external sources, but even so, most of the environment should be contained (or controlled, whatever you want to call it)
* **Think about the worst cases**. Yes, this is mostly the reason why we start writing tests. What happens if I get an unlisted data for my switch? What happens for empty lists or empty arrays? What happens if the user enters an emoji? What happens when the user doesn't speak english and it writes a non-english character? What's the limit for the text? What happens if the image server is down? etc. You should always think on the bad scenarios first and write tests for them. You'll get surprised about the results of writing this kind of tests
* **Think about the good cases**. Once you fix all the issues with bad scenarios your code should still work properly and if not, then you should fix the code again to satisfy the bad and the good scenarios. Remember, **Hope for the best, prepare for the worst**
* **Think how others will use your code**. When writing your tests think about how others will invoke your functions, which data will they use for that call, and what they are expecting as a result. Try to write your tests with this in mind, as real calls to your code and what could happen if someone sends the wrong *parameters* to your functions. Think about this scenarios in two perspectives: _**other developers**_ and _**final users**_. Will a final user always enter his password right at first sight? Will a developer understand at first that complex `Response` object you return when they call your `getProfile()` function? Is `Response` the right name for that class? these are only examples of things you can face when writing tests, while trying to figure out if everything works as expected or if you will need to change things
* **Put scenarios in your mind**. Create the whole scenario of a user and check which parts of your code are involved, then write that scenario as a test and validate that everything works as the scenario states it will
* **Don't be afraid of changing things**. When writing tests you will notice that probably doing a call to a function is not that simple and you need a lot of data to perform that call. Well, write the test in a "*happy path*" way as you would expect it to be done, and then do a refactor of the code to work as the test says. Most of the time, you will end up adapting the code to pass the test, never the other way around. If you adapt a test to pass without changing the code, probably (most definitely) you are writing a  _false-positive_
* **Don't overthink your tests**. There is a moment for best practices and complex code. Writing tests is not that moment. A test should be straightforward about their intentions. It should be readable in a way that anyone without context of the code being tested can understand the intention of the said test

## Conclusion

Testing is as complex as writing code… because testing is also writing code. And if you like writing code, you should like writing tests as well, and validate that your code works as expected. **Testing is not that hard**, but it requires time and practice. In the end, it is as easy as writing regular code and will teach you more about your own code and what are the next steps for it.

There's still a lot more to say about testing and I'll be writing about it in future articles, and trying to update this one properly with links to other articles and examples. 

Thanks for reading!