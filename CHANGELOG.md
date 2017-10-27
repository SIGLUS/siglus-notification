3.0.4 / WIP
==================

Bug fixes, security and performance improvements (backwards-compatible):
* [OLMIS-3394](https://openlmis.atlassian.net/browse/OLMIS-3394): Added notification request validator
  * from, to, subject and content fields are required and if one of them will be empty the endpoint will return response with 400 status code and error message.

3.0.3 / 2017-09-01
==================

Bug fixes, security and performance improvements (backwards-compatible):
* [OLMIS-2871](https://openlmis.atlassian.net/browse/OLMIS-2871): The service now uses an Authorization header instead of an access_token request parameter when communicating with other services.
* [MW-412](https://openlmis.atlassian.net/browse/MW-412): Added support for CORS.

3.0.2 / 2017-06-23
==================

New functionality added in a backwards-compatible manner:
* [OLMIS-2611](https://openlmis.atlassian.net/browse/OLMIS-2611): Added using locale from env file.

3.0.1 / 2017-05-08
==================

Bug fixes, security and performance improvements (backwards-compatible):

* [OLMIS-1972](https://openlmis.atlassian.net/browse/OLMIS-1972): Update Postgres from 9.4 to 9.6
* Update [Docker Dev Image](https://github.com/OpenLMIS/docker-dev) for builds from v1 to v2
