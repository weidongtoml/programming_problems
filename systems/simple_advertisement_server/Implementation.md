# Implementation Details

This document provides a brief overview of the current implementation adserver,
a simple advertisement servicing service.


## Overview

Adserver is a HTTP REST service for serving advertisement based on the current time.
The main entry point is [ad_server.py](./bin/ad_server.py). It is responsible for:
* loading server configuration from [ad_server_conf.json](./conf/ad_server_conf.json)
using [ad_server_config.py](./bin/ad_server_config.py), see [_load_config](./bin/ad_server.py#L63-L65) 
* load a pre-specified set of advertisements from the [ads directory](./ads) using
[ad_content_updater.py](./bin/ad_content_updater.py), see [_load_ads](./bin/ad_server.py#L52-L61)
* create a server end point to listen for request, see [__init__](./bin/ad_server.py#L29)

Incoming requests are being routed by [handle_get](./bin/ad_server.py#L67-L79) method,
which, based on the path, invoke the appropriate request handler.

The core functionality is handled by [_get_ad](./bin/ad_server.py#L87-L91) method,
which parses the url for parameters and convert them to arguments acceptable by 
[AdSearcher](./bin/ad_search.py#L19), taking care of only client from privileged IPs
can access certain extended functionality. After AdSearcher returns the list of 
eligible advertisement, it then packaged them into a JSON object and return the result
to the client, having stripped away the unnecessary and potentially sensitive information.

It also handles health check via the [_get_status](./bin/ad_server.py#L120) method
and report internal states via the [_get_states](./bin/ad_server.py#L130) method.


## Advertisement selection

Advertisement selection is done by the [ad_search.py](./bin/ad_search.py) module.
The list of advertisements is first scanned through and processed into a sorted list
by expiration date in [__init__](./bin/ad_search.py#L13-L17), we also create dictionary
that maps a given advertisement Id to the advertisement. 

To search the list of advertisement Ids that falls is eligible for current time
in [search_ad_ids](./bin/ad_search.py#L19) method, we first use binary search via 
[_get_active_ads_start_index](./bin/ad_search.py#L40) to filter out the already
expired advertisements. Then, depending on whether we want to return all of the
advertisement even if they have started or not, we will iterate the rest of the
array to locate them. 

Note that to support multi-time zone, all time specifications are converted
to number of seconds since the Epoch in [UTC](https://en.wikipedia.org/wiki/Coordinated_Universal_Time) 
using [convert_to_timestamp](./bin/ad_format.py#L38) function, before being processed by the system. 
In this way, we standardize our timezone as well as removing the issue of day light saving.


## Advertisement store format

To make our file for storing advertisement easily extensible and readable, JSON has
been selected. The [load_json](./bin/generic_json_loader.py#L7) method is provided
to make it easily to specify the required JSON format. And in the case of the
advertisement format, it is specified in a simple Python dictionary 
[AD_FILE_FORMAT](./bin/ad_format.py#L52), where we specified the name of the fields
and the types, e.g. integer or string. And in the case where it is not a simple
type and would require a complexity checking and conversion, we also can specify
a function to check and transform the JSON input into the desirable object, in
this case, it is used to parse the text datetime and timezone into the actual
campaign period, see [_display_period_parser](./bin/ad_format.py#L9).

As a side note, the [AdServerConfig](./bin/ad_server_config.py#L17) also uses this
facility to load server configuration.

Note that it is important for our format to be extensible, that only the required
fields are checked and not to reject fields that are present but not required.
A case arise is how to do we deploy new format. Since we allow extra information
in the current ad format, we can safely deploy new banner_ads.json file to production
containing the fields to be used in the future without fear that it is incompatible
with existing production code. Then we update the production code to read these
new fields and make use of them. Deleting existing fields means we will do the reverse,
update our production code to remove reference to the deprecated fields, then publish
the new banner_ads.json file with the deprecated fields removed.


## Debugging

The concept of [privileged IPs](./conf/ad_server_conf.json) is introduced so that
the client can access various debugging information, including requesting all 
advertisements that have not expired at a specified date, inclduing the ones that
are not yet started.


## Testing

The [tests directory](./tests) should contain a set of regression tests to be run
to ensure the correctness of the APIs. 

