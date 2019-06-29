# composer-satis-update

#Complementary tool to Satis for updating the satis.json "require" key from the project composer.json.

This is particularly useful if you are mirroring for git repositories and package zip files (dist files).

##Problem description
If you use in satis.json ``"require-all": true`, you will have all versions of all packages in the repositories you defined which can take a lot of disk space

OR

You can choose to manually maintain the "require" key which can be time-consuming if you have a lot of packages.

##Usage
java -jar satis-update.jar
    [-c|--composerjson <composer>] Path to the project composer.json file
    [-a|--authjson <authJson>] Path to the auth.json file 
    [-g|--giturl <gitUrl>] Base git url (example https://github.com)
    [-h|--satishostname <satisHostName>] Satis host name (example satis.dev.com)
    [-j|--satisjsonpath <satis>] Path to the satis.json configuration file
Example
Given

satis.json

{
    "name": "My Repository",
    "homepage": "http://localhost:7777",
    "repositories": [
        { "type": "vcs", "url": "https://github.com/mycompany/privaterepo" },
    ],
    "require": {
    }
}
and

composer.json

{
    "name": "mycompany/mycompany-project",
    "require": {
        "mycompany/privaterepo": "^1.3"
    },
    "repositories": [
        {
            "packagist": false
        },
        {
            "type": "composer",
            "url": "http://localhost:7777/"
        }
    ]
}
and Composer Satis Builder is installed:

php composer.phar create-project aoe/composer-satis-builder --stability=dev
After running

java -jar satis-update.jar -c /path/to/composer.json -g github.com -h satis.dev.com -j /path/to/satis.json

satis.json will look like:

{
    "name": "My Repository",
    "homepage": "http://localhost:7777",
    "repositories": [
        { "type": "vcs", "url": "https://github.com/mycompany/privaterepo" },
    ],
    "require": {
        "mycompany/privaterepo": "^1.3"
    },
}
Now build Satis as before:

php bin/satis build satis.json web/
License
Composer Satis Builder is licensed under the MIT License - see the LICENSE file for details
