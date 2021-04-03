# camel-experiments
This project is a repository for some experiments using Apache Camel.

## camel-google-storage-test
The first experiment is camel-google-storage-test that shows how to integrate with Google Cloud Storage using Apache Camel.
The use case is an application that creates thumbnail images from a bucket, store it to a target bucket and created the relative Download URL.
To run the example:

```bash
cd camel-google-storage-test
$env:GOOGLE_APPLICATION_CREDENTIALS = 'C:\keys\my-key.json'
mvn camel:run
```

