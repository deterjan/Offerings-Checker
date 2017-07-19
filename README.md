# Offerings-Checker

This is a course quota tracker for Bilkent University courses. It sends periodical requests to Bilkent STARS to 
obtain the remaining quota for a given course and notifies the user when the quota is greater than zero.

# Setup

This project requires [only minimal configuration and tweaking](https://xkcd.com/1742/) to build and use.

1. First, obtain JAR files for JSoup and JavaMail from the following links and add them as libraries to your project.
https://jsoup.org <br/>
http://www.oracle.com/technetwork/java/javamail/index.html

2. Obtain the security certificate from https://stars.bilkent.edu.tr and add it to Java cacerts. See the following link:
  https://www.grim.se/guide/jre-cert <br/>
  This is required because the root issuer of Bilkent's certificate is TUBITAK which Java doesn't trust by default.

3. (Optional) To use sound notifications obtain a sound file that is preferably of the .wav format (I haven't tested for other 
formats) and shorter than 30 seconds. Rename this file to "alert.wav" and place it inside your project's resources folder.

4. (Optional) To use mail notifications, open sidemodules/MailBot.java and modify the final strings username and password to 
those of a GMail e-mail address. The account you will use must have [allowed access from less secure apps](https://support.google.com/accounts/answer/6010255?hl=en).

5. That's it. I too wish it was easier to set up ¯\\\_(ツ)\_/¯  
