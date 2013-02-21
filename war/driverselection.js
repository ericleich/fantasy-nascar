/* Driver Selection js */
var DRIVER_LIMIT = 5;
var POINT_LIMIT = 600;
var drivers = {};
var points = 0;
function updateSelection(input, name, value) {
  if (input.checked) {
    drivers[name] = value;
    points += value;
  } else if (drivers[name]) {
    delete drivers[name];
    points -= value;
  }
  var html = "";
  for (var driver in drivers) {
    html += driver + " - " + drivers[driver] + "<BR/>";
  }
  document.getElementById('currentDrivers').innerHTML = html;
  document.getElementById('totalValue').innerHTML = points;
  validatePicks();
}

function selectDriver(driver) {
  var name = driver.name;
  var input = document.getElementById(name);
  input.checked = true;
  var value = driver.value;
  updateSelection(input, name, value);
}

function validatePicks() {
  var submit = document.getElementById('submit');
  var numKeys = getNumKeys(drivers);
  var valid = numKeys <= DRIVER_LIMIT &&
              numKeys > 0 &&
              points <= POINT_LIMIT;
  submit.disabled = !valid;
}

function getNumKeys(obj) {
  var numKeys = 0;
  for(var key in obj){
    if (obj.hasOwnProperty(key)) {
      numKeys++;
    }
  }
  return numKeys;
}

/** Implementing HttpRequests to populate drivers */
function sendMessage(path, opt_param)
{
  if (opt_param)
  {
    path += '?' + opt_param;
  }
  var xhr = new XMLHttpRequest();
  xhr.open('GET', path, true); // true means asynchronous
  xhr.onreadystatechange = function() {
    if (xhr.readyState == 4) {
      selectDrivers(xhr.responseText)
    }
  };
  xhr.send();
}

function selectDrivers(responseJSON)
{
  console.log(responseJSON);
  var selectedDrivers = JSON.parse(responseJSON);
  for (var index in selectedDrivers) {
    var selectedDriverObject = selectedDrivers[index];
    var name = selectedDriverObject.name;
    var rank = selectedDriverObject.rank;
    var score = selectedDriverObject.score;
    var driver = new Driver(name, rank, score);
    selectDriver(driver);
  }
};

function Driver(name, rank, score) {
  this.name = name;
  this.rank = rank;
  this.score = score;

  this.values = [190, 180, 175, 165, 160, 155, 150, 145, 140, 135,
                 132, 128, 124, 121, 118, 115, 112, 109, 106, 103,
                 100, 97, 94, 91, 88, 85, 82, 79, 76, 73,
                 70, 67, 64, 61, 58, 55, 52, 49, 46, 43,
                 40, 37, 34, 10];
  this.value = rank < 44 ? this.values[rank-1] : 10;
}

function getDrivers() {
  sendMessage('/driver', 'type=selected');
}
/** END OF Implementing HttpRequests to populate drivers */