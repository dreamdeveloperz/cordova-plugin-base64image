var Base64Image = function () {
};
Base64Image.prototype.saveImage = function (successCallback, errorCallback, base64String, params) {
    cordova.exec(successCallback, errorCallback, "Base64ImagePlugin", "saveImage", [base64String, params]);
};


if (!window.plugins) {
    window.plugins = {};
}
if (!window.plugins.Base64Image) {
    window.plugins.Base64Image = new Base64Image();
}
