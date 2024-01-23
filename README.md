[markdown](documentation/markdown/index.md)
[html](documentation/html/index.html)
[https://raw.githubusercontent.com/BaeWooRam/BiometricTest/blob/2d18cb8e7f13f97322071777ee3c41f05c5d0f60/documentation/html/index.html](https://raw.githubusercontent.com/BaeWooRam/BiometricTest/2d18cb8e7f13f97322071777ee3c41f05c5d0f60/documentation/html/app/com.geekstudio.biometrictest/index.html)

{% include https://github.com/BaeWooRam/BiometricTest/blob/2d18cb8e7f13f97322071777ee3c41f05c5d0f60/documentation/html/index.html %}

{% for post in site.posts %}
    <a href="{{ post.url }}">
        <h2>{{ post.title }} &mdash; {{ post.date | date_to_string }}</h2>
    </a>
    {{ post.content }}
{% endfor %}

```{r showChoro1}
htmltools::includeHTML("./animations/demographics.html")
```

---
title: "Introduction"
author: "chinsoon12"
date: "April 10, 2016"
output: html_document
---

<<insertHTML:[documentation/html/index.html]

etc, etc, etc

```{r, echo=FALSE}
htmltools::includeHTML("documentation/html/index.html")
```

etc, etc, etc

https://htmlpreview.github.io/?https://github.com/BaeWooRam/BiometricTest/blob/2d18cb8e7f13f97322071777ee3c41f05c5d0f60/documentation/html/index.html
