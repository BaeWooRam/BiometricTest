[markdown](documentation/markdown/index.md)
[html](documentation/html/index.html)
https://github.com/BaeWooRam/BiometricTest/blob/2d18cb8e7f13f97322071777ee3c41f05c5d0f60/documentation/html/index.html
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
