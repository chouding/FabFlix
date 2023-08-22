let paymentForm = $("#payment-form")

function postOrder(movie_id, amount){
    let status = ""
    const data = paymentForm.serialize() + "&movie_id=" + movie_id + "&amount=" + amount
    console.log(data)

    $.ajax(
        "api/payment", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            async: false,
            data: data,
            success: function(result){
                status = result["status"]
            }
        }
    );
    return status
}


function handlePlaceOrder(event){
    event.preventDefault();
    const keys =  { ...localStorage }
    let isBreak = false
    for (let key in keys){
        console.log(key)
        const value = parseInt(localStorage[key])
        const status = postOrder(key, value)
        console.log(status)
        console.log(status, status === "success")

        if (status !== "success"){
            $("#error-msg").text("Wrong Credit Card Information")
            isBreak = true
            break
        }
    }
    if (isBreak){
        return
    }
    else{
        console.log("out")
        window.location.replace("payment-confirmation.html");
    }
    return false
}


function getTotal(key, value){
    const result = jQuery.ajax({
        dataType: "json",
        method: "GET",
        async: false,
        url: `api/shopping-cart?id=${key}&amount=${value}`,
        success: function(resultData) {}
    }).responseText


    const price = $.parseJSON(result)[0]["total_price"]
    return parseFloat(price)

}

function displayTotal(){
    let total_price = 0
    for (let i = 0; i < localStorage.length; i++){
        const key = localStorage.key(i)
        const value = localStorage[key]
        console.log(key, value)
        const price = getTotal(key, value)
        total_price += price
    }

    const str = "Total: " + total_price + ""
    $("#Total").text(str)
}

displayTotal()
paymentForm.submit(handlePlaceOrder)
