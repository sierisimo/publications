const express = require("express");

const app = express();

var x = 1;

app.get("/", (req, res) => {
    console.log("Hello Server.");
    res.send("Hi");
});

app.get("/info", (req, res) => {
    let info = {
        "id": 123,
        "name": "Sier",
        "elements": [{"inner": true}]
    };

    console.log("Output JSON will have property: ", x % 2 === 0);
    if (x % 2 === 0) {
        x++;
        info["optional"] = "Added";
    } else {
        x--
    }

    console.log("Sending JSON…");
    console.log(info);
    console.log("--------");

    res.json(info);
});

app.listen(9000);