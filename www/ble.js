"use strict";

module.exports = {
    print: function (message, success, failure) {
        cordova.exec(success, failure, 'BLE', 'print', [message]);
    }
};

