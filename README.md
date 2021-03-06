# OpenLMIS Notification Service
This repository holds the files for OpenLMIS Notification Independent Service.

## Prerequisites
* Docker 1.11+
* Docker Compose 1.6+

## Quick Start
1. Fork/clone this repository from GitHub.

 ```shell
 git clone https://github.com/OpenLMIS/openlmis-notification.git
 ```
2. Add an environment file called `.env` to the root folder of the project, with the required 
project settings and credentials. For a starter environment file, you can use [this 
one](https://raw.githubusercontent.com/OpenLMIS/openlmis-ref-distro/master/settings-sample.env). e.g.

 ```shell
 cd openlmis-notification
 curl -o .env -L https://raw.githubusercontent.com/OpenLMIS/openlmis-ref-distro/master/settings-sample.env
 ```
3. Develop w/ Docker by running `docker-compose run --service-ports notification`.
See [Developing w/ Docker](#devdocker).
4. You should now be in an interactive shell inside the newly created development 
environment, start the Service with: `gradle bootRun`
5. Go to `http://<yourDockerIPAddress>:8080/` to see the service name 
and version. Note that you can determine yourDockerIPAddress by running `docker-machine ip`.
6. Go to `http://<yourDockerIPAddress>:8080/api/` to see the APIs.

## Building & Testing
See the Building & Testing section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#building.

## <a name="devdocker">Developing with Docker</a>
See the Developing with Docker section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#devdocker.

### Development Environment
See the Development Environment section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#devenv.

### Build Deployment Image
See the Build Deployment Image section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#buildimage.

### Publish to Docker Repository
TODO

### Docker's file details
See the Docker's file details section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#dockerfiles.

### Running complete application with nginx proxy
See the Running complete application with nginx proxy section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#nginx.

### Logging
See the Logging section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#logging.

### Internationalization (i18n)
See the Internationalization section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#internationalization.

### Debugging
See the Debugging section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#debugging.

## Environment variables

Environment variables common to all services are listed here: https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#environment-variables

This service also uses the following variables:

* **MAIL_HOST** - The hostname or IP of the SMTP server that will be used for sending outgoing email. For example: smtp.example.com. This variable has to be defined in order to send emails. 
* **MAIL_PORT** - The SMTP port to use for sending outgoing email. For example 25 or 465. This variable has to be defined in order to send emails. The default is 25.
* **MAIL_USERNAME** - The SMTP username to use for sending outgoing email. Usually required by the SMTP server.  
* **MAIL_PASSWORD** - The SMTP password to use for sending outgoing email. Usually required by the SMTP server.
* **MAIL_ADDRESS** - The sender email address that will be used for sending all outgoing email messages (the from-address field). For example set it to noreply@mydomain.org in order for users to see that as the sender of the email they receive. Note that some email providers (like Gmail) might overwrite this value with details from your account.   

See [SMS integration](#sms-integration) below for environment variables for SMS integration.

## Production by Spring Profile

By default when this service is started, it will clean its schema in the database before migrating
it. This is meant for use during the normal development cycle. For production data, this obviously
is not desired as it would remove all of the production data. To change the default clean & migrate
behavior to just be a migrate behavior (which is still desired for production use), we use a Spring
Profile named `production`. To use this profile, it must be marked as Active. The easiest way to
do so is to add to the .env file:

```java
spring_profiles_active=production
```

This will set the similarly named environment variable and limit the profile in use.  The
expected use-case for this is when this service is deployed through the
[Reference Distribution](https://github.com/openlmis/openlmis-ref-distro).

### Demo Data
A basic set of demo data is included with this service, defined under 
`./src/main/resources/db/demo-data/`.  This data may be optionally loaded by using the `demo-data` 
Spring Profile.  Setting this profile may be done by setting the `spring.profiles.active` 
environment variable.

When building locally from the development environment, you may run:

```shell
$ export spring_profiles_active=demo-data
$ gradle bootRun
```

To see how to set environment variables through Docker Compose, see the 
[Reference Distribution](https://github.com/openlmis/openlmis-ref-distro)

## <a name="sms-integration">SMS Integration</a>

The Notification Service now has integration with SMS through an SMS provider. This integration has 
been designed with RapidPro/TextIt in mind, but as long as the SMS provider provides access to a 
REST API (using a token) from which to send SMS messages, it should be supported.

To enable SMS integration, two settings need to be set in the .env file:

* **SMS_SEND_API_URL** - The REST API URL of the SMS provider that will be used for sending SMS 
messages. (Example: https://textit.in/api/v2/broadcasts.json) This variable has to be defined in 
order to send SMS messages. 
* **SMS_SEND_API_TOKEN** - The API access token to use for the REST API. This variable has to be 
defined in order to send SMS messages.

Note: make sure that all users that are supposed to receive SMS messages have phone numbers set in 
their user contact details, and that these numbers are in E.164 format (e.g. for US numbers, 
12065551234).
