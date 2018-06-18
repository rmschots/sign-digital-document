const PROXY_CONFIG = {
    "/api/*": {
        "target": "http://localhost:7654",
        "secure": false,
        "logLevel": "debug",
        "pathRewrite": {
            "^/api": ""
        }
    }
};

module.exports = PROXY_CONFIG;
