const hsl = require('./nodevm');

exports.generate = (req, res) => {

    hsl(req.query.req).then(data => {
        res.status(200).send(data);
    })

};
