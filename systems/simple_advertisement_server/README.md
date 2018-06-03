# Simple Advertisement Server

A minimalistic banner advertisement server which provides a HTTP REST API for retrieving 
banner advertisements based on the current time.


## Rationale for a stand-alone service

There are many advantages for implementing the advertisement retrieval functionality
as a RESTful web service as compared to a library component:
* it can be deployed independently from existing system, reducing the risk of bugs in 
this component to affect existing service, as well as allowing independent development,
testing, and deployment.
* the REST API is language agnostic, so it does not matter what language the existing
system is using.
* there are many existing tools can be used, e.g. using HAProxy for load balancing.
* it can be independently scaled up or down from the rest of the system depending on
the traffic.
(There are certainly more advantages, interested reader can research more on the topics
of micro-services.)

Of course, there is no free lunch. The extra cost associated with an independent REST
service instead of a library include but not limited to:
* integration would required adding network code instead of just a simple function call
* extra communication overhead: each request needed to be sent over the network instead
of just a local function call.
* an extra service to be provisioned

Given an how important an advertisement service's role in many online businesses, 
especially for those where it is a major source of revenue, it would certainly receive
major investment in its development. As such, velocity in implementing and releasing
new features will be crucial, and eventually the current simple system will become
very complex. Hence, it is important that we lay a good foundation at the very
beginning to support future growth.

Interested reader can find further discussion in Future Work section on the potential
evolution of this system.


## Features

* Easily deployable, the only requirement is having Python 2.7 installed.
* Minimalistic but complete, it loads ad contents from a given JSON file (hence forth refered
as the advertisement store) and provides a rest API for selecting the advertisements based on 
the current time.
* Supports multiple time zone specification of the banner advertisements.
* Provides endpoint for checking server internal states as well as current status, and
only access to such sensitive information for a list of white-listed IPs only 
(hence forth will be referred as privileged IPs).
* Allow privileged IPs to override the current time as well as whether to return all
active advertisements for online trouble shooting.
* Easily extensible: the advertisement store can be easily extended using a configurable schema.


## Quick start

A quick way to start playing with this service is to simply checkout the code, ensure that
python 2.7 is installed, then simply run
```
mkdir ./logs
python ./bin/ad_server.py
```
The above should start a service instance locally on port 8000.
The following links can be used to confirm if the setup is successful:
* [health](http://localhost:8000/internal/status])
* [internal state](http://localhost:8000/internal/states])
* [retrieve ads](http://localhost:8000/api/get_ad?dt=20000101:12:00:00&tz=UTC&all=1)

Diagnostic messages can be found in `./logs/adserver.log`.


## API

* Advertisement retrieval
    * Retrieving a list of eligible advertisements:
        ``` 
        curl http://localhost:8000/api/get_ad 
        ```
        this will return a JSON object containing the list of advertisements that can 
        be displayed at this moment. Sample output is as follow:
        ```
        [
            {
                "id": 23232, 
                "img": "http://advertisement_host/path_to_banner_image_1.png"
            },
            {
                "id": 32123,
                "img": "http://advertisement_host/path_to_banner_image_2.png"
            }
        ]
        ```
    * Retrieving a list of eligible advertisement for a specified time 
    (available to privileged clients only):
        ``` 
        curl http://localhost:8000/api/get_ad?dt=20000101:12:00:00&tz=UTC 
        ```
        similar to the above, this will returned the list of eligible ads, but instead of
        using the current time as selection criteria, the service will use the specified time,
        in this case, it is `2000-01-01 12:00:00 UTC`.
    * Retrieving all active advertisements
     (available to privileged clients only):
        ``` 
        curl http://localhost:8000/api/get_ad?all=1 
        ```
        this will return all advertisements that are currently active, i.e. end date is in the future,
        including the ones with campaign starting in a future date.
    * The above parameters `dt`, `tz` and `all` can be combined to retrieve all advertisements that
        was / will be valid starting from the given time, e.g.
        ``` 
        curl http://localhost:8000/api/get_ad?dt=20000101:12:00:00&tz=UTC&all=1 
        ```
        will treat the current time as `2000-01-01 12:00:00 UTC` and return all advertisements that
        was active or will be active after this date.
* Checking service status (available to privileged clients only)
    * to check if the current service is ready, use
        ``` 
        curl http://localhost:8000/internal/status 
        ```
        if the service is healthy, it will return `{"status": "ok"}`, 
        if it returns `{"status": "outage"}`, it means the service is encountering issues and requests
        should not be forwarded to this instance.
      This is useful for monitoring production service health to alert for human intervention as well
      as load balancing.
      (Currently implementation only check if the service is able to properly load the advertisement store)
    * to check service internal state, use
      ``` 
      curl http://localhost:8000/internal/states 
      ```
      This will return a JSON object giving details of the internal states.
      (Current implementation will return the list of advertisements loaded.)
      Having access to the internal state of the service is essential to diagnose 
      production issues such as why a given advertisement is not displaying.


## Advertisement store

The advertisement store is a directory for storing the set of advertisements the service
should load and serve. By default, it is located in `./ads` directory. 
Currently it should contain 2 files, `banner_ads.json` which contains the list of banner 
advertisement in JSON format, `banner_ads.md5` which contains the md5 checksum for `banner_ads.json`
file. 

The checksum file `banner_ads.md5` is needed to guard against file corruption during network transfer. 
The service will only attempt to load advertisements from `banner_ads.json` after verifying
its checksum against the one stored in `banner_ads.md5`. (On Mac OS, it can be generated using 
```cat banner_ads.json | md5 > ./banner_ads.md5``` .)

The actual advertisement store file is `banner_ads.json`, its format is specified by 
`Ad_FILE_FORMAT` in [ad_format.py](./bin/ad_format.py).
Currently it is required to have the following fields:

| field name    | field value      | purpose |
| ------------- | ---------------- | ------- |
| version       |  integer         | Current version of the advertisement set stored in this file. |
| pub_timestamp |  integer         | Unix timestamp at the time this set of advertisements was generated. |
| ad_content    |  list of objects | The list of advertisements. |

Each advertisement object stored in `ad_content` field mentioned above is of the format
```
{
  "id": <unique ad id>,
  "content": {
    "img_url": <url to banner image>
  },
  "display_period": {
    "start": <campaign start period of the format YYYYmmdd:HH:MM:SS>,
    "end": "<campaign end time, format is the same as "start">,
    "time_zone": <time zone code, e.g. "UTC">
  }
}
```
Included in the distribution is a sample [banner_ads.json](./ads/banner_ads.json) as an example
for how the advertisements are organized.


## Configuration

The service can be configured by modifying [ad_server_conf.json](./conf/ad_server_conf.json) in
the `conf` directory.

| Field name     | Purpose |
| -------------- | ------- |
| server_host    | The host name for the service to bind to. |
| server_port    | The port for the service to bind to for listening for incoming request. |
| privileged_ips | List of IP addresses that have the privilege to access restricted APIs. |


## Deployment

Current version provides very little support for deployment as it is still lacking many features.
Future version may include the necessary scripts for deploying to production.
Here, we will discuss what a potential setup could look like.

For our advertisement service to be production ready, we first and foremost need to ensure its
availability. To achieve this, we should deploy this service on multiple hosts 
(2 or more instances on different machines), so that if one machine goes down, we still have a
backup. Since current implementation does not support online loading of new advertisement set,
we will have to restart an instance for it to pick up the latest advertisement set. So 
redundancy is needed to ensure that during restart, we are still able to provide the service 
by the other instance. Of course, having multiple instances also allow us to support more traffic than a single instance
can handle, allowing us to scale horizontally. 

We will need to setup a mechanism to transfer the latest advertisement set to each of the
hosts. A simple way to do this is to designate a single machine as the master, which contains
the latest up to date copy of advertisements. A cron-job will be setup on this master to periodically
synchronise the advertisements to each host, e.g. using rsync. Of course, due to the lack of
online advertisement store re-load, we will need to send command to the remote host to restart
the adservice instance so it can pick up the latest advertisement. At this point, it is crucial
for this to be done one after the other to ensure not all instances are restarted at the same 
time to avoid total service outage.

Two or more proxies, e.g. HAProxy should be deployed so that clients can have a set of fixed IPs
to talk to for accessing our services. This is needed so that external clients are shielded away
from the irrelevant operational details such as adding / removing / moving service instance. 
It also provide a cheap away of service discovery (i.e. client only need to know the proxy IPs but not the actual
host of the adservice instance.) We will also need to set up the proxies to periodically conduct health-checks on
our adservice instances, so that we do not direct traffic to non-functional instances.

We will need monitoring facility in place to ensure we are notified of any system outage, apart from doing periodic
health-check on the adservice instances, we will also need to monitor the number of requests going to each instance,
the time in takes for each queries to be fulfilled. Whether all the instances have the latest advertisement set, etc.
Of course, also monitor that our proxies are up at all time as they are the interface to external source.

We have only touched what the deployment look like, and obviously we will need to automate the process of deploying
new instances, or even creating a new cluster. Updating new version of our service, updating the proxies to let them
know of the instances added or removed, etc. We will not cover them here for keep this document brief and also these
are general issues faced by any system.


## Future work

The following are a list of ares that needed to be work on to make our advertisement system to be enterprise level:

### Logging

Currently only minimal logging been added. In the future, we will need to add logging for timing of fulfilling each
requests as well as what each request is. 

### Advertisement reloading

Current implementation requires the service instance to restart in order to pick up the latest advertisement set,
we will need to change this. An easy way to do this is having a separate thread to periodically check if the
`brand_ads.md5` checksum file has been modified since last time the advertisements have been loaded. If it has been
modified, then attempt to load the advertisement. 

### Monitoring

We will to expose more service internal metrics such as current request load, average response time, etc. Or maybe
just use out of the box solution such as DataDog.

### Advertisement distribution

As mentioned in [Deployment] section, we should probably provide a way to distribute advertisements out of the box
for easy cluster setup.

### Scaling

Current implementation is single thread, which means it will not be able to utilize the multi-cores in modern servers.
We could of course, set up multiple instances in the same host, but we will have multiple copies of the advertisements
in memory, which is very wasteful. 
In the future, if a single host can not store all advertisements in memory, we will need a way to optimize memory usage
such as storing non-active ads on disk, or sharding the instances.

### Different advertisement model

Currently we are only supporting advertisements to be selected based on current time. Which means we can only sell
our advertisement inventory in the unit of time period, which may not result in the best use of our inventories.
We certainly want the advertisements we show to users to be useful to them instead of just spams, in this case
we will need to focus on performance of display. Instead of charging by the time period, we might want to change
our product to charge by performance, such as clicks, or better still conversions. In this case, we will need to
keep track which advertisement impressions were performing, e.g. led to clicks on the advertisement, or a conversion 
of a good sold. 

The first step to achieve the above would be having correct data, we will need a system to track displays, clicks,
conversions of each advertisement impressions, as well as any other information related to this. Then utilize these
information, train a ranking model to better evaluate an advertisement's usefulness to a given user. Then during
a serving process, we will return only the most relevant advertisements. Note that there will be a lot of things involve,
this only touches the most basic aspect.

On the other hand, sometimes advertisers might not care about clicks or conversions, they just want to display their
message to a given audience for a period of time (e.g. Facebook advertisements), and trust that even having a glimpse 
of the advertisements would influence the target audience. If this is the business model, then we will need to be able 
to categorize users into different groups, provide a way to specify the targeting criteria of each advertisements. 
And since it will be impression based charging, we will need to find a way to control impressions for each. It is 
likely that an advertiser would like their advertisement to be displayed evenly across a given period for a given
amount of impressions purchases, it would certainly be bad experience for all impressions to be delivered within
the first hour. Here, we will need a feedback system to quickly tell us how many impressions for an advertisement
has been delivered, as well as predicting its future delivery rate so that we can better control its delivery.
    
Both of the above would mean a better budgeting system, i.e. each advertisement would have a budget of impressions,
clicks, or conversions, so that if the budget is spent, we should stop showing the given advertisement.

### Advertisement management system

We have been discussing the delivery system of advertisement, but a crucial part of a successful advertisement system
is the management of advertisements. This is the set of applications that are used by advertiser to create and manage
their advertisement campaigns and monitor their progress. It also include reports for advertisement to evaluate
their campaign performance. We will also need to consider how to support A/B testing for advance users, where they
want to figure out what the best set of advertisement materials is to achieve the desired result. The importance
of the accuracy and timeliness of performance reports can never be overstated. Doing it well is expected, but any
discrepancy would erode customer trust. Third-party tracking would also need to be supported so that clients can
use external service to validate what we claim to deliver. 

### Billing facilities

Last but not least, is the billing facility needed to be in place so that our custom can easily purchase inventory
on their own. Without this, the business would need to invest a lot in customer service and sales to handle this
aspect of the operation. Here, accuracy as well as security is paramount. Even if we use third-party services, 
integration on our side would also required careful consideration.


## About the author

Weidong has been involved with building advertisement system for almost a decade. 
His work spanned click-through-rate prediction model development and improvement in one of China's largest
tech companies [Tencent](https://en.wikipedia.org/wiki/Tencent), processing terebytes of data to extract useful
features to enable machine learning model to better understand what advertisement a given user would find
interesting; to creating and deploying model prediction clusters in China's second largest eCommerce website 
[JingDong](https://en.wikipedia.org/wiki/JD.com), the platform enabled CTR, CVR and other models to be easily
rolled out to production to be experimented on live traffic, supporting linear logistic model, 
factorization machines model, as well as deep neural network. More recently, he led a team
at [Indeed.com](https://en.wikipedia.org/wiki/Indeed) built a new advertisement offering to help employers to
raise their brand awareness among job-seekers to help people find the job they love.

