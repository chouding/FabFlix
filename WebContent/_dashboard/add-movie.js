function handleSubmitForm(event){
    console.log("Submit add movie")
    event.preventDefault();
    $.ajax(
        "api/add-movie", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            async: false,
            data: $("#add-form").serialize(),
            success: function(result){
                $('#info-message').append('<p>' + JSON.parse(result)["message"] + '</p>')
            }
        }
    )
}



$("#add-form").submit(handleSubmitForm)
