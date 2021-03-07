# Healthy Heart

## Inspiration
High blood pressure is the #1 risk factor for stroke and a major risk factor for heart disease. It’s often called the silent killer because you can’t feel it - it’s up to you to track your blood pressure and maintain a healthy lifestyle to control it. Over 70% of men aged 55 and up are classified as having hypertension and 1 in 3 American adults have high blood presssure. It is such a common health concern and there are many resources online that provide information about high blood pressure.

Today, the internet provides a large wealth of resources where individuals can find information for just about anything. However, this amount of information has also put up a barrier for our seniors in preventing them from accessing reliable resources, especially when it comes to their health. For us, it’s as simple as doing a quick Google search. But for someone who was not exposed to technology for the majority of their lives, it's difficult to even break down what doing a google search means. 

## What it does
Healthy heart is a simple and informative web application to help seniors understand their blood pressure. 

## How we built it
Healthy heart is built using Python Flask and uses the OpenCV library to livestream the photo footage and capture an image. A request is sent to the Google Cloud Vision API which detects the text in the image and interprets it as the user’s blood pressure. The results are returned to the application and depending on the user’s blood pressure, it displays the appropriate advice and provides them with more resources and way to help control and maintain a healthy blood pressure.

## Challenges we ran into
At first, the goal was to develop an android app, however, due to our limited experience, it was not viable due to the time constraints. Instead, we decided to develop a web app as a first prototype.

## Accomplishments that we're proud of
This is my first time developing the full stack for an application which I am really proud of.

## What we learned
I learned how to develop a Python Flask project and implementa Google API.

## What's next for Healthy Heart
What I’ve developed this weekend is just a base prototype that contains minimal information for blood pressure interpretation. The next steps for healthy heart, would be to research and put the resources directly on the application and provide more information on interpreting results. In the future, it would also be helpful to develop this into a mobile application to make it even easier for seniors to use. All they would need is a little help in downloading the app, and then the resource will be available on their phones whenever they check their blood pressure.
