"use strict";
// Constants and Variables Declaration

function getFormattedDate(d) {
    var year = d.getFullYear();
    var month = parseInt(d.getMonth()) + 1;
    var day = d.getDate();
    return year + '-' + month + '-' + day;
}

function setDateTimeCheck() {
    var d1 = new Date();
    var d2 = new Date();
    if(d1.getMonth() === 0) {
        d1.setMonth(12);
        d1.setFullYear(d1.getFullYear() - 1);
    }
    else d1.setMonth(d1.getMonth() - 1);
    
    if(d2.getMonth() === 12) {
        d2.setMonth(0);
        d2.setFullYear(d2.getFullYear() + 1);
    }
    else d2.setMonth(d2.getMonth() + 1);
    
    $(".dateTime").dateTimePicker({
        mode: 'date',
        format: 'yyyy-MM-dd',
        limitMin: getFormattedDate(d1),
        limitMax: getFormattedDate(d2),
    });
    console.log('listening');
}

function validateTimestamp() {}

setDateTimeCheck();