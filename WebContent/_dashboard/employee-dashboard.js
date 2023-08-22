function populateTable(resultData, table_name){
    let bodyElement =jQuery("#"+table_name);
    for (let i = 0; i < resultData.length; i++){
        const attribute = resultData[i]["column_name"]
        const type = resultData[i]["type"]
        let rowHTML = "<tr>";
        rowHTML += "<th>" + attribute+ "</th>";
        rowHTML += "<th>" + type + "</th>";
        rowHTML += "</tr>";
        bodyElement.append(rowHTML)
    }

}


function createTable(resultData){
    let bodyElement = jQuery("#meta-info-tables");

    for (let i = 0; i < resultData.length; i++) {
        const table_name = resultData[i]['name']
        const table_str_html =
            "<h2>" + table_name + "</h2>\n"+
            "<table class=\"table\" id=\"" + table_name + "\" >\n" +
            "    <thead>\n" +
            "    <tr class=\"TableTitle\">\n" +
            "        <th scope=\"col\">Attribute</th>\n" +
            "        <th scope=\"col\">Type</th>\n" +
            "    </tr>\n" +
            "    </thead>\n" +

            "    <tbody id=movie_table_body ></tbody>\n" +
            "</table>"

        jQuery.ajax({
            dataType: "json",  // Setting return data type
            method: "GET",// Setting request method
            url: "api/table-info?table-name="+table_name, // Setting request url, which is mapped by StarsServlet in Stars.java
            success: (resultData) => populateTable(resultData, table_name) // Setting callback function to handle data returned successfully by the SingleStarServlet
        })
        console.log(table_str_html);
        bodyElement.append(table_str_html);
    }
}


function initTable(){
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/table-names", // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => createTable(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
    })
}

initTable();