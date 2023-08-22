/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}


function handleCartClick(e){
    const current_amount = localStorage.getItem(e.target.id)
    if (current_amount == null){
        localStorage.setItem(e.target.id, "1")
    }
    else{
        localStorage.setItem(e.target.id, String(parseInt(current_amount)+1))
    }
    alert("Added item in to shopping cart!\nCurrent amount in shopping cart: "
        + localStorage.getItem(e.target.id))
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // find the empty h3 body by id "star_info"
    let movieInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<p> " + resultData[0]["movie_title"] + "</p>");

    console.log("handleResult: populating movie table from resultData");


    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < 1; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[i]["movie_title"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";

        let genresArray = resultData[i]["movie_genres"].split(", ");
        rowHTML += "<th>"
        for (let j = 0; j < genresArray.length; j++) {
            rowHTML +=
                '<a href="result.html?genres=' + genresArray[j] + '">' +
                genresArray[j] +
                '</a>';
            if (j < genresArray.length - 1) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>"
        let starsArray = resultData[i]["movie_stars"].split(" , ");
        let starIdsArray = resultData[i]["star_ids"].split(" , ");
        rowHTML += "<th>"
        for (let j = 0; j < starsArray.length; j++) {
            rowHTML +=
                '<a href="single-star.html?id=' + starIdsArray[j] + '">' +
                starsArray[j] +
                '</a>';
            if (j < starsArray.length - 1) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>"
        rowHTML += "<th>" + (resultData[i]['movie_rating'] == '0.0' ? 'N/A' : resultData[i]['movie_rating']) + "</th>";
        rowHTML += "<th><button id=" + resultData[i]['movie_id'] + " onclick='handleCartClick(event)'>ADD</button></th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
    const resultUrl = resultData[1]["resultUrl"]
    $("#result").attr("href", resultUrl.indexOf("url=") === -1 ? "index.html" :
                                resultUrl.slice(resultData[1]["resultUrl"].indexOf("url=")+4));
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});