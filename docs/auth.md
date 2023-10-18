Authentication
==============

Configuration
-------------

The `dd-sword2` service can be configured to authenticate users in the following ways:

* Basic authentication (username/password) against a list of users in the configuration file. To add users add sections to the
  `userSettings.users` section in the configuration file. For example:

         userSettings:
            users:
            - name: user001
              passwordHash: $2a$10$yvmSYczU7z4KL6qmRCTgTeSvo7uurwPUbB9s/mTKzJrYM/sQKgF.y
              collections:
               - collection1
              filepathMapping: true
  As you can see, this section also configures other settings for the user, such as the collections the user is allowed to deposit
  in. If the passwordHash key is present it will be used to authenticate the user. The value must be the user´s password hashed
  with bcrypt.

* Delegating authentication a remote service, by forwarding specific headers to that service. This is used for users that are not
  in aforementioned list, or are in the list but without the passwordHash key. The remote service is configured in the settings for
  the default user:
            
        userSettings:
            default:
               passwordDelegate:
                  url: 'http://localhost:20340/'
                  forwardHeaders:
                  - Authorization
                  - X-Dataverse-key

               collections:
               - collection1
               filepathMapping: true

 


