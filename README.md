TwitterLinkPrediction
=====================

Combining techniques from network science like supervised random walks and natural language processing we predict which interactions will happen on Twitter. This application uses the algorithm proposed by Backstrom and Leskovec in a [recent paper](http://dl.acm.org/citation.cfm?id=1935914). The goal is to understand what factors are important in forming new social and economic bonds.

![alt tag](https://raw.githubusercontent.com/gajduk/TwitterLinkPrediction/master/TwitterLinkPrediction/drawing.png)

In order to get better predictions we integrate both network data (followers) and text data (tweets). So far we have looked at activity patterns, hashtag usage, tweet content, retweet frequency, mentions and how they influence befriending. 
