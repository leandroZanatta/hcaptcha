const HSWController = require('./hsw.controller');

exports.routesConfig = function (app) {
    app.get('/hsw', [HSWController.generate]);
};