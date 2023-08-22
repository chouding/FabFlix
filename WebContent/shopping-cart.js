function handleDeleteClick(e){
    const movie_id = e.target.id
    localStorage.removeItem(movie_id)
    updateShoppingCart()

}

function handleAdd(e){
    const movie_id = e.target.id
    const new_value = parseInt(localStorage[movie_id]) + 1
    localStorage[movie_id] = new_value.toString()
    updateShoppingCart()
}

function handleSubtract(e){
    const movie_id = e.target.id
    const new_value = parseInt(localStorage[movie_id]) - 1
    localStorage[movie_id] = new_value.toString()
    if (localStorage[movie_id] === "0"){
        localStorage.removeItem(movie_id)
    }
    updateShoppingCart()
}

function setTableContent(jsonObj, key, value){
    const table = jQuery("#shopping-cart-tbody")
    let rowHTML = ""
    rowHTML += "<tr id=" + key + ">";
    rowHTML += "<th scope=\"row\">" +
        '<a href="single-movie.html?id=' + key+ '">' + jsonObj["title"] +
        '</a>' + "</th>"
    rowHTML += "<th> <button id=" + key + " onclick='handleSubtract(event)' class='button2'>-</button> " + value +
                    "<button id=" + key + " onclick='handleAdd(event)' class='button2'>+</button></th>";
    rowHTML += "<th><button id=" + key + " onclick='handleDeleteClick(event)'  class='button1'>DELETE</button></th>";
    rowHTML += "<th>" + jsonObj["price"]  + "</th>";
    rowHTML += "<th>" + jsonObj["total_price"]   + "</th>";
    rowHTML += "<tr>";
    table.append(rowHTML)
    return jsonObj["total_price"]
}

function handleTableContent(key, value){
    const result = jQuery.ajax({
        dataType: "json",
        method: "GET",
        async: false,
        url: `api/shopping-cart?id=${key}&amount=${value}`,
        success: function(resultData) {setTableContent(resultData[0], key, value)}
    }).responseText


    const price = $.parseJSON(result)[0]["total_price"]
    return parseFloat(price)

}


function updateShoppingCart(){
    const table = jQuery("#shopping-cart-tbody")
    let keys = []
    table.empty()
    for (let i = 0; i < localStorage.length; i++){
        const key = localStorage.key(i)
        keys.push(key)
    }
    keys.sort()

    let total_price = 0
    for (let i = 1; i < keys.length; i++){
        const key = keys[i]
        const value = localStorage[key]
        const price = handleTableContent(key, value)
        console.log(price)
        total_price += price
    }

    const str = "Total: " + total_price.toFixed(2) + ""
    $("#Total").text(str)
}

function toPayment(){
    if (localStorage.length === 0){
        alert("Shopping cart is currently empty. Please add a movie before proceeding")
    }
    else{
        window.location = "payment.html"
    }
}

updateShoppingCart()
$("#submit-cart").click(toPayment)