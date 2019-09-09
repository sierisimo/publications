---
title: The Function that changed my mind
published: false
description: How a single function show me a lot of kotlin 
tags: kotlin,functions,android,refactor
cover_image:
series: 
---

When writting code we should always have a place for improvement. Improvement can present itself in different ways or forms. A proof of it is how in recent years the big attention to CI (_Continous Integration_) and CD (_Continous Delivery_) has grown. Same goes for new languages and technologies.

But not all involve changing to a new language, technology or tool. Some of these improvements are simply changing how something works, adding tests, adding abstractions, changing patterns, etc. This is the case for the change I introduced on one of my projects.

**Disclaimer**: If you know me in person or have been in a meetup with me presenting, there's a big chance you already heard me talking about this, even [on YouTube with the GDG Santo Domingo]() or Kotlin/Everywhere Guadalajara and Mexico City.

**Another disclaimer**: This article is not about architecture –even when it's a hot topic in the Android community– or best practices or even about how to organize an Android Aplication, it's just about a set of changes that showed me some concepts of Kotlin. Also, I started this changes almost 3 years ago, by the point of publication of this article, probably the libraries from Google –like `core-ktx`– already have something similar or better. And just to be clear: None of the approaches discutted here is right or wrong, all the solutions depend on individual needs and individual reasons to choose them over the others.

## But first… some Context

If you're an Android Developer or have worked with Android, probably you catch the pun intended in the joke, if not, let me explain you quickly.

In Android, everything happening on your application has a `Context` and to make some stuff work, you also need a `Context`. This is just a class that Android uses to give information and access to the app.

One specific case is when you want to start a new `Activity` on your app. This means –in short– launching a new whole screen or launching an external application. To do this, you first create an `Intent` based on your `Context` and pass the `Actiity` class you want to start:

```kotlin
val myIntent = Intent(context, MyNextActivity::class.java)
```

Then you can start `MyNextActivity` from another `Activity` simply using this intent: `startActivity(myIntent)`

If your application relays on activities for showing different sections/views of your application then probably you will end with a lot of this calls distributed around. In some different variants, but all of them with the same structure:

```kotlin
val someIntent = Intent(context, SomeActivity::class.java)
//Do stuff with the intent
startActivity(someIntent)

//And if you don't do things with the intent and you are in an activity
startActivity(
    Intent(this, MyNextActivity::class.java)
)
```

## My first problem

To be honest, there is no problem with this code. Not at all. But I have seen this happening in a lot of applications, you start having these calls distributed around without control, then you suddenly have 2 or 3 places doing the same, setting the same intent with a different variable name but same parameters and then doing the same call. To solve these, most people usually do one of two things:

1. Wrap it around a single method for each case
2. Put all under a single class/object and just pass needed parameters for repeated cases

This means, for case 1:

```kotlin
/** Inside your class that will have the calls **/
fun startNext() {
    startActivity(
        Intent(this, MyNextActivity::class.java)
    )
}

fun startSome() {
    val someIntent = Intent(this, SomeActivity::class.java)
    //Do things with intent
    startActivity(someIntent)
}
```

And for case 2:

```kotlin
object AppRouter {
    fun startNext(context: Context) {
        startActivity(
            Intent(context, MyNextActivity::class.java)
        )
    }

    fun startSome(context: Context) {
        val someIntent = Intent(context, SomeActivity::class.java)
        //Do things with intent
        startActivity(someIntent)
    }
}
```

You can add different layers of complexity but this is the base for most solutions: **have a single point for launching/starting new activities**. In my case I always ended having case 1 all around. Having functions here and there seemed like a good solution, until I ended with 120 functions doing almost the same all around, which is not bad but seeing that `::class.java` even inside of different methods really bother me plus seeing 3 or 4 functions inside the same class with almost the same body also feels wrong.

I wanted a way to remove that, to make that `::class.java` disappear from everywhere and have something more explicit or at least more readable without the 123193 wrapper functions all around.

## Generics

Coming from _Java Land_ and seeing that all my function looked the same with one single parameter changed made me thing: "I can do better thant this". So I did:

```kotlin
fun <T> launchActivity(context: Context, Class<T> clazz) {
    val intent = Intent(context, clazz)
    startActivity(intent)
}
```

First problem solved: We have a single function to start our activities and we can leave it as **a global function**. As long as someone has access to a `Context` it can make use of this function. But…

1. Uncle Bob says that the less parameters, the better the function –and it's true, is easier to use it–
2. The `::class.java` will still be present all around
3. If we want to hide the `::class.java` and provide a more "business" related abstraction we still have a lot of functions

```kotlin
fun startNext(context: Context) {
    launchActivity(context, MyNextActivity::class.java)
}

fun startSome(context: Context) {
    launchActivity(context, SomeActivity::class.java)
}
```

And seeing this… well, the initial stuff that triggered me was the whole `::class.java` and is still present, this is not a great improvement…

## Inheritance

Well, having this **global function** also feels kind of wrong, and almost all the places using the function will be some kind of `Activity`. Maybe we can make the function available through inheritance…

```kotlin
open class BaseLaunchActivity: Activity() {
    fun <T> launchActivity(Class<T> clazz) {
        val intent = Intent(this, clazz)
        startActivity(intent)
    }
}

//And then…
class MyActivity : BaseLaunchActivity() {
    fun onSomeClick() {
        launchActivity(SomeActivity::class.java)
    }
}
```

Cool! We got rid of one parameter, we hide where the magic is happening and made the function available to most of the parts using it.

But wait… _most_? We want **all**, not _most_… also this has other issues:

1. Is not always possible to inherit
2. Not all the classes in need of the code will be a kind of `Activity`
3. When some class has access to `Activity` it will need to cast in an ugly way
4. The `::class.java` is still present –ugh, it's ugly–

For the casting part we will have:

```kotlin
(getActivity() as BaseLaunchActivity).launchActivity(SomeActivity::class.java)
```

Which is **TURBO-SUPER-ULTRA DANGEROUS** –yes, I'm over reacting– because you can't be sure that from now and all the time, this cast will be successful. Why? Well, your class/component is depending on a container and the class itself does not have a mechanism to check all the time which type the container has –and even if it has it, it shouldn't be designed in that way– also if your functionality depends on the container being of some type… well, that's bad design.

## Extension functions to the rescue

Depending on inheritance for this simply functionality seems like producing more problems. Other approach will be using an interface:

```kotlin
interface ActivityLauncher {
    fun <T> launchActivity(context: Context, Class<T> clazz) {
        val intent = Intent(context, clazz)
        startActivity(intent)
    }
}
```

But we got back to having 2 parametes… BUT on interfaces we can enforce properties:

```kotlin
interface ActivityLauncher {
    val launcherContext: Context

    fun <T> launchActivity(Class<T> clazz) {
        val intent = Intent(launcherContext, clazz)
        startActivity(intent)
    }
}
```

But now every class that needs this function will need to both:

1. Implement the interface
2. Give a value to the `launcherContext` property

That's boring!!

What about a single extension function to remove the dependency over the property?

```kotlin
fun <T> Context.launchActivity(Class<T> clazz) {
    val intent = Intent(this, clazz)
    startActivity(intent)
}
```

That's actually cool!

Now whatever `Context` existing around can simply invoke this function without issues, we no longer need wrapper functions but… the `::class.java` has not moved. Arghhh… that thing is making our code look ugly…

And we have a bigger problem… turns out that for this small function, the Kotlin compiler will generate some bytecode that once existing in the JVM will look aproximately –on java– like this:

```java
public final class ContextExtensions {
    public static final void launchActivity(@NotNull Context context, @NotNull Class clazz) {
        Intent intent = new Intent(context, clazz);
        startActivity(intent);
    }
}
```

This is not bad but is a caveat of having Kotlin extension functions and… well, if is only to make things readable, we are not making too much progress, we only removed 1 line of code from our original block of code. What can save us from actually having this on the bytecode and still adding readability to our codebase?

## INLINE

Adding a single reserved word to our function will make everything cool on the bytecode:

```kotlin
inline fun <T> Context.launchActivity(Class<T> clazz) {
    val intent = Intent(this, clazz)
    startActivity(intent)
}
```

But if you are writting this on **AndroidStudio** or **IntelliJ** probably you'll notice a warning explaining that there's no actual optimization from _inlining_ this function. And that's true, `inline` works as an **_SMART-COPY-PASTE_** which mean it will do magic on compile time to paste the body of your function where is being called with actual params and stuff.

Talking about params… what about the `Intent`? We usually pass information between activities using the `Intent` right? With the current approach is not possible to pass any parameter… let's fix that first and we then can check the warning from the `inline`.

## Lambda with reciever

Because we don't want to increment the size of our parameter list, because the types of parameters can grow and because we don't want to have multiple wrapper functions just for the parameters, we need to find a way to "_configure_" or `Intent` _on demand_.

The _lambda with reciever_ is one way to fix this. Adding this parameter to our function we can allow each caller to configure during execution of our function the `Intent` and still keep things readable:

```kotlin
inline fun <T> Context.launchActivity(Class<T> clazz, confBlock: Intent.() -> Unit) {
    val intent = Intent(this, clazz)
    intent.confBlock()
    startActivity(intent)
}
```

Adding this last parameter we are saying that our function can take a lambda that will execute in the scope of an `Intent` and can use properties and methods from the `Intent`. This lambda is later used to setup our `Intent` and finally we start the activity with the already configured `Intent`.

This change allows the execution to be something like:

```kotlin
fun onSomeInteraction() {
    launchActivity(SomeActivity::class.java) {
        putExtra(MY_KEY, myValue)
        putExtra(SOME_KEY, someValue)
    }
}
```

Also, the warning about `inline` disappears with these changes as the optimization now makes sense. In compile time, the compiler will take the call to our function and replace it with the actual body of the function and replacing the lambda with a local variable. This will increase a little bit the size of our bytecode but gave us more readability for usages.

The bytecode for this call on Java will look something like:

```java
public void onSomeInteraction() {
    Intent intent = new Intent(this, SomeActivity.class);
    intent.putExtra(MY_KEY, myValue);
    intent.putExtra(SOME_KEY, someValue);
    startActivity(intent);
}
```

As you can see, no reference to external classes, no calls to external functions, all living there. This is a good thing, as the code stays simple while coding and has the real meaning when executing.

But still… while codign the `::class.java` is present. Why I have been complaining about this if is something present in Kotlin? Well, the main reason is because this operator and class is not nice, it's quite ugly and also can be confusing. There are more than one way to obtain a `Class<T>` from a class and this one is the easier one. There's a chance for erro using the other ones and they are verbose, not what we are trying to avoid here.

## Reified for your types

We are currently having a generic – `<T>` – function that is _inlined_, these two elements can be combined to use a third element on our function: `reified`: 

```kotlin
inline fun <reified Ty> Context.launchActivity(Class<T> clazz, confBlock: Intent.() -> Unit) {
    val intent = Intent(this, clazz)
    intent.confBlock()
    startActivity(intent)
}
```

Adding this word will means that for every single call of our function, when inlining, the compiler will use the actual type instead of a generic or a super type in the caller. This small change allows us to use the generic inside of our function as a real type, then we can remove the first parameter and replace with the real type:

```kotlin
inline fun <reified T> Context.launchActivity(confBlock: Intent.() -> Unit) {
    val intent = Intent(this, T::class.java)
    intent.confBlock()
    startActivity(intent)
}
```

Now all the calls to this function can be as clean as:

```kotlin
launchActivity<SomeActivity> {
    putExtra(MY_KEY, myValue)
}

launchActivity<MyNextActivity> {}
```

Some final touches to the signature will make the function even safer and even cooler when being called:

1. Make `T` have a constraint to `Activity` in a way only classes that extend from `Activity` are allowed
2. Add a default value to the _lambda with reciever_ to allow calls with empty configuration

```kotlin
inline fun <reified T: Activity> Context.launchActivity(confBlock: Intent.() -> Unit = {}) {
    ...
}
```

Now our function is cooler than ever, won't add too much to the final bytecode and also will make our code more readable. Wow! We traveled a long way doing a lot of changes to a single piece of code to make things cooler, but not just that, now other people developing along with us can benefit from this improvement as they can simply call our function without having to worry how it works. I have seen even people adding animations, making a way to hold results and transforming data using this kind of functions/approaches. Writting idiomatic Kotlin actually makes you think and develop different and cooler!

## Conlusion

We can apply this kind of approch by steps or go directly to other functions on our codebase, one example is the usage of `SharedPreferences` and making the _edition_ do an _auto-apply_ "magically" using the lambda with reciever. We can create ways to configure more stuff in less steps and this will make our writting of code improving as we are adding tools to our set of _already-vaste_ set of functions and tools on kotlin. Thinking on what is repeated or what can be improved allows us –developers– to discover new worlds and make things cooler and sharper. Don't be scared of the change.
