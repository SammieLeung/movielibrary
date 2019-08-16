package com.firefly.dlna.httpserver;

interface IRegistrationFactory {
    Registration[] generate(String uri, String mimeType);
    Registration[] generate(String uri);
}
