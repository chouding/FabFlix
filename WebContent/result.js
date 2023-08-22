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

function handleResult(resultData) {
    let starTableBodyElement = jQuery("#result-tbody");

    // Dynamically modify `Result` url based on search query
    $("#result").attr("href", resultData[0]["resultUrl"]);

    for (let i = 0; i < resultData.length-1; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">' +
            resultData[i]['title'] +
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]['year'] + "</th>";
        rowHTML += "<th>" + resultData[i]['director'] + "</th>";
        let genresArray = resultData[i]["genres"].split(", ");
        rowHTML += "<th>";
        for (let j = 0; j < genresArray.length; j++) {
            let capitalizedGenre = genresArray[j].charAt(0).toUpperCase() + genresArray[j].slice(1)
            rowHTML +=
                '<a href="result.html?genres=' + capitalizedGenre + '">' + capitalizedGenre + '</a>';
            if (j < genresArray.length - 1) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>"
        let starsArray = resultData[i]["stars"].split(" , ");
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
        rowHTML += "<th>" + (resultData[i]['rating'] == '0.0' ? 'N/A' : resultData[i]['rating'])  + "</th>";
        rowHTML += "<th><button id=" + resultData[i]['movie_id'] + " onclick='handleCartClick(event)'>ADD</button></th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);

    }

    // Create pagination buttons
    handlePagination(resultData[0]['num_movies']);
}

function handlePagination(num_movies) {
    let paginationElement = jQuery("#pagination");
    let prevNextHTML = "";
    let url = window.location.href;
    if (url.includes("page")) {
        let i = (url.slice(-1) == "&") ? 1 : 0
        url = url.slice(0, url.indexOf("page")-1+i)
    }

    const pageNum = parseInt(page)
    const maxPageNum = Math.ceil(parseInt(num_movies) / parseInt(limit ? limit : 25))

    prevNextHTML += '<a' + (page > 1 ? (' href="' + url + '&page=' + (pageNum-1) + '">') : '>') + '&laquo; Prev</a>'
    prevNextHTML += ' ' + pageNum + ' / ' + maxPageNum + ' '
    prevNextHTML += '<a' + (page < maxPageNum ? (' href="' + url + '&page=' + (pageNum+1) + '">') : '>') + 'Next &raquo;</a>'

    paginationElement.append(prevNextHTML);
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

let title = getParameterByName('title');
const year = getParameterByName('year');
const director = getParameterByName('director');
const star = getParameterByName('star')
let genres =  getParameterByName('genres')
const prefix = getParameterByName('prefix')
const limit = getParameterByName('limit')
const sort = getParameterByName('sort')
let page = getParameterByName('page')
let url = window.location.href

if (genres == null) {
    genres = $("#genres").val();
} else {
    $("#genres").val(genres);
}
if (title == null) {
    title = $("#title").val();
} else {
    $("#title").val(title);
}

if (limit != null) {
    $("#limit").val(limit)
}
if (sort != null) {
    $("#sort").val(sort)
}

if (page == null) {
    page = $("#page").val();
} else {
    $("#page").val(page);
}

resetPage = () => { page = 1; $("#page").val(1); }

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: `api/search?title=${title}&year=${year}&director=${director}&star=${star}` +
         `&genres=${genres}&prefix=${prefix}&limit=${limit}&sort=${sort}&page=${page}` +
         `&url=${url}`,
    success: (resultData) => handleResult(resultData)
});