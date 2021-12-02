class Point {
    constructor(x, y, b) {
        this.x = x;
        this.y = y;
        this.b = b;
    }
}

let dots = [];
let d;
let indexi = 0;
let indexj = 1;
let dmin = Number.POSITIVE_INFINITY;
let count = 0;
let button;

function setup() {
    //COLORES
    createCanvas(windowWidth, windowHeight);
    background(255);
    button = createButton('submit');
    button.position(windowWidth - 100, 50);
    button.mousePressed(greet);
}

function greet() {
    lines();
}

/*function windowResized() {
    resizeCanvas(windowWidth, windowHeight);
}*/

function mouseClicked() {
    fill(0);
    ellipse(mouseX, mouseY, 5, 5);
    let dot = new Point(mouseX, mouseY, 0);
    dots.push(dot);
    count++;
    console.log(dots[dots.length - 1]);
    //lines();
}

async function lines() {
    if (dots.length >= 2) {
        for (i = 0; i < dots.length - 1; i++) {
            for (j = i + 1; j < dots.length; j++) {
                console.log(i, j);
                d = Math.sqrt(Math.pow((dots[i].x - dots[j].x), 2) + Math.pow((dots[i].y - dots[j].y), 2));
                stroke(0);
                if (d < dmin) {
                    dmin = d;
                    indexi = i;
                    indexj = j;
                }
                line(dots[i].x, dots[i].y, dots[j].x, dots[j].y);
                await sleep(500);
            }
        }
    }
    console.log(dots)
    stroke(255, 0, 0);
    line(dots[indexi].x, dots[indexi].y, dots[indexj].x, dots[indexj].y);
    await sleep(500);
    console.log(dmin);
    console.log(new Point(dots[indexi].x, dots[indexi].y, dots[indexi].b));
    console.log(new Point(dots[indexj].x, dots[indexj].y, dots[indexj].b));
}

// custom helper function to deliberately slow down
// the sorting process and make visualization easy
function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}