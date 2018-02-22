# Capstone-Project

Capstone project for the Udacity´s Android course

### Prerequisites

We need to specify 2 Google API keys: One for Google Maps API and other one for Google Geocode Webservice. We specify them in the build.gradle (app):

For DEBUG version:
```
debug {
	...
	resValue "string", "google_maps_api_key", "[DEBUG_GOOGLE_API_KEY]"
	resValue "string", "google_maps_api_key_for_webservice", "[DEBUG_GOOGLE_API_KEY_FOR_WS]"
}
```

For RELEASE version:
```
release {
	...
	resValue "string", "google_maps_api_key", "[RELEASE_GOOGLE_API_KEY]"
	resValue "string", "google_maps_api_key_for_webservice", "[RELEASE_GOOGLE_API_KEY_FOR_WS]"
}
```

## Author

* **Peter Arcentales Trujillo**