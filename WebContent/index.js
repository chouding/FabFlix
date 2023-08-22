
function appendAlphabet(){
    const categories = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p',
        'q','r','s','t','u','v','w','x','y','z',0, 1, 2, 3, 4, 5, 6, 7, 8, 9,'*']
    const browse = document.getElementsByClassName("browse")[0]
    const header = document.createElement("h2")
    header.innerHTML  = "Browse By Title"
    browse.append(header)

    const alphabet_links = document.createElement("div")
    alphabet_links.setAttribute("class", "alpha-list")
    for (let i = 0; i < categories.length; i++){
        const newAlph = document.createElement("a")
        newAlph.href = "result.html?" + "prefix=" + categories[i]
        newAlph.text = categories[i]
        alphabet_links.appendChild(newAlph)
    }

    browse.appendChild(alphabet_links)
}


function handleGenreResult(resultData){
    const browse = document.getElementsByClassName("browse")[0]
    const header = document.createElement("h2")
    header.innerHTML  = "Browse By Genres"
    browse.append(header)

    const genres_links = document.createElement("div")
    genres_links.setAttribute("class", "genres-list")
    for (let i = 0; i < Math.min(resultData.length); i++) {
        const name = resultData[i]['name'];
        const newGenre = document.createElement("a")
        newGenre.href = "result.html?" + "genres=" + name
        newGenre.text = name
        genres_links.appendChild(newGenre)
    }
    browse.appendChild(genres_links)
    appendAlphabet()
}

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/genres", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleGenreResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

