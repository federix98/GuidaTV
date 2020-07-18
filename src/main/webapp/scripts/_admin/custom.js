"use strict";
// Constants and Variables Declaration
let base_path = $('#base_path').attr('data-val');
let edit_buttons_prototype = $('#edit_buttons_prototype');
let table_id = ($(".mydata").length > 0) ? $(".mydata").attr("id") : "undefined";
let id_modal = "#editModal";
let delete_modal = "#deleteModal";
let date_times = ['input[name=start]'];
let my_url = location.protocol + '//' + location.host + location.pathname;
var target;
// Helper functions

// POTEVO ASSEGNARE DIRETTAMENTE IL VALORE DI day = ''
// QUI MA INTERNET EXPLORER NON è COMPATIBILE
function my_datatable_build(path, id_elem, day) {
    //console.log(path, "path");
    //console.log(id_elem, "id_elem");
    var elem = $('#' + id_elem);
    if (elem.length > 0) {
        $('#' + id_elem).dataTable({
            "paging": true,
            "pageLength": 10,
            "autoWidth": false,
            "lengthChange": true,
            "searching": false,
            "processing": true,
            "serverSide": true,
            "ajax": {
                url: path,
                type: 'GET',
                data: {
                    day: day
                }
            },
            "columnDefs": [
                {
                    "targets": -1,
                    "data": null,
                    "defaultContent": edit_buttons_prototype.html()
                }
            ]
        });
    }
}

function fill_edit_modal(source, id_modal, data_id) {
//console.log(data_id, "fill data");
    $.ajax({
        url: source,
        type: "POST",
        data: {
            'data_id': data_id,
            'edit': true
        },
        success: function (result) {
            //console.log(result);
            $(id_modal).find(".modal-content").html(result);
            $(id_modal).modal('show');
            setDateTimeCheck();
        },
        error: function (xhr) {
            console.log(xhr, "ajax error edit modal");
        }
    });
}

function insert_item_modal(source, id_modal) {
//console.log(data_id, "fill data");
    $.ajax({
        url: source,
        type: "POST",
        data: {
            'insert': true
        },
        success: function (result) {
            $(id_modal).find(".modal-content").html(result);
            $(id_modal).modal('show');
            setDateTimeCheck();
        },
        error: function (xhr) {
            console.log(xhr, "ajax error edit modal");
        }
    });
}

function store_item(source, data, multipart) {
    data.append('store', true);
    if (!multipart) {
        var object = {};
        data.forEach(function (value, key) {
            object[key] = value;
        });
        data = object;
        $.ajax({
            url: source,
            type: "POST",
            data: data,
            cache: false,
            success: function (result) {
                console.log(result, "result store");
                var res = JSON.parse(result);
                //console.log(res);
                if (res.errors) {
                    $('#forErrors').removeClass("d-none");
                    $('#forErrors').html(res.errors);
                }
                if (res.success === "true") {
                    location.reload();
                }
            },
            error: function (xhr) {
                console.log(xhr, "ajax error store");
            }
        });
    } else {
        $.ajax({
            url: source,
            type: "POST",
            data: data,
            cache: false,
            processData: false,
            //contentType: "multipart/form-data;charset=UTF-8",
            contentType: false,
            success: function (result) {
                console.log(result, "result store");
                var res = JSON.parse(result);
                //console.log(res);
                if (res.errors) {
                    $('#forErrors').html(res.errors);
                    $('#forErrors').removeClass("d-none");
                }
                if (res.success === "true") {
                    location.reload();
                }
            },
            error: function (xhr) {
                console.log(xhr, "ajax error store");
            }
        });
    }

}

function delete_item(source, data_id) {
    $.ajax({
        url: source,
        type: "POST",
        data: {
            'data_id': data_id,
            'delete': true
        },
        success: function (result) {
            console.log(result);
            var res = JSON.parse(result);
            //console.log(res);
            if (res.errors) {
                console.log(res.errors);
            }
            if (res.success === "true") {
                location.reload();
            }
        },
        error: function (xhr) {
            console.log(xhr, "ajax error edit modal");
        }
    });
}

function setDateTimeCheck() {
    $(".dateTime").dateTimePicker({
        mode: 'dateTime',
        format: 'yyyy/MM/dd HH:mm:ss'
    });
    console.log('listening');
}

function validateTimestamp() {}

function destroyDT(id_elem) {
    $('#' + id_elem).DataTable().destroy();
}

// MANAGEMENT PROGRAMMAZIONI
my_datatable_build(my_url, table_id, '');
// EDIT MODAL SHOW
$(document).on('click', '._edit', function (event) {
    event.preventDefault();
    var id_element = $(this).closest("tr").children("td:eq(0)").children("div").html();
    fill_edit_modal(my_url, id_modal, id_element, false);
});
// DELETE LISTENER
$(document).on('click', '._delete', function (event) {
    event.preventDefault();
    target = $(this).closest("tr").children("td:eq(0)").children("div").html();
    $(delete_modal).modal('show');
});
$('#deleteItemForm').submit(function (event) {
    event.preventDefault();
    delete_item(my_url, target);
});
// SUBMIT LISTENER INSERT AND UPDATE
$(document).on('submit', '#storeForm', function (event) {
    event.preventDefault();
    var multipart = false;
    if ($(this).attr("enctype") === "multipart/form-data") multipart = true;
    var data = new FormData(this);
    /*for (var value of data.values()) {
        console.log(value); 
    }*/
    store_item(my_url, data, multipart);
    return false;
});
$('.addButton').click(function (event) {
    event.preventDefault();
    insert_item_modal(my_url, id_modal);
});

if($('#filterDay').length > 0) {
    console.log('esiste')
    $('#filterDay').click(function (event) {
        console.log('clicked')
        event.preventDefault();
        var day = '';
        if($('#day').length > 0) day = $('#day').val();
        destroyDT(table_id);
        my_datatable_build(my_url, table_id, day);
    });
}
