function init_search(){
    function handleClick(){
        const title = document.getElementById("title-input").value;
        const year = document.getElementById("year-input").value;
        const director = document.getElementById("director-input").value;
        const star = document.getElementById("star-input").value;

        window.location.href = `result.html?title=${title}&year=${year}&director=${director}&star=${star}`

    }
    const element= document.getElementsByClassName("search")[0]

    const input1 = document.createElement("input")
    input1.type = "text"
    input1.id = "title-input"
    input1.placeholder = "title"

    const input2 = document.createElement("input")
    input2.type = "text"
    input2.id = "year-input"
    input2.placeholder = "year"

    const input3 = document.createElement("input")

    input3.type = "text"
    input3.id = "director-input"
    input3.placeholder = "director"

    const input4 = document.createElement("input")
    input4.type = "text"
    input4.id = "star-input"
    input4.placeholder = "star"

    const button = document.createElement("button")
    button.id = "submit";
    button.onclick = handleClick;
    button.innerHTML = "Submit"

    element.appendChild(input1)
    element.appendChild(input2)
    element.appendChild(input3)
    element.appendChild(input4)

    element.appendChild(button)
}

init_search()