function setTableContent(jsonObj, key, value){
    const table = jQuery("#confirmation-tbody")
    let rowHTML = ""
    rowHTML += "<tr id=" + key + ">";
    rowHTML += "<th>" +
        '<a href="single-movie.html?id=' + key + '">' + jsonObj["title"] +
        '</a>' + "</th>"
    rowHTML += "<th>" + value + "</th>";
    rowHTML += "<th>" + jsonObj["total_price"]   + "</th>";
    rowHTML += "<tr>";
    table.append(rowHTML)
    console.log("append")
}

function handleTableContent(key, value){
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: `api/shopping-cart?id=${key}&amount=${value}`,
        success: function(resultData) {setTableContent(resultData[0], key, value)}
    });
}



function updateConfirmation(){
    const table = jQuery("#confirmation-tbody")
    table.empty()
    for (let i = 0; i < localStorage.length; i++){
        const key = localStorage.key(i)
        const value = localStorage[key]
        handleTableContent(key, value)
    }

    while (localStorage.length > 0){
        console.log("delete")
        const key = localStorage.key(0)
        localStorage.removeItem(key)
    }

}

updateConfirmation()