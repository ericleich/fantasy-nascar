var DRIVER_LIMIT = 5;
var POINT_LIMIT = 600;
var selectedDrivers = {};
var points = 0;

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

function getUpdatedDrivers() {
  var teamSelect = document.getElementById("teamselect");
  var raceSelect = document.getElementById("raceselect");
  var params = 'item=drivers&raceid=' + raceSelect.value;
  sendMessage('/admin/api', params, createDriversTable);
}

/** Implementing HttpRequests to populate drivers */
function sendMessage(path, opt_param, callback)
{
  if (opt_param)
  {
    path += '?' + opt_param;
  }
  var xhr = new XMLHttpRequest();
  xhr.open('GET', path, true); // true means asynchronous
  xhr.onreadystatechange = function() {
    if (xhr.readyState == 4) {
    	callback(xhr.responseText);
    }
  };
  xhr.send();
}

function createDriversTable(responseJSON) {
  var tableBody = "";
  var drivers = getDrivers(responseJSON);
  var numDrivers = drivers.length;
  var numRows = Math.ceil(numDrivers / 4.0);
  var index = 0;
  for (; index + 3*numRows < numDrivers; index++) {
    tableBody += "<tr>" + getDriverInputHtml(drivers[index]) + "<td></td>"
                        + getDriverInputHtml(drivers[index + numRows]) + "<td></td>"
                        + getDriverInputHtml(drivers[index + 2*numRows]) + "<td></td>"
                        + getDriverInputHtml(drivers[index + 3*numRows])
                        + "</tr>";
  }
  for (; index < numRows; index++) {
    tableBody += "<tr>" + getDriverInputHtml(drivers[index]) + "<td></td>"
                        + getDriverInputHtml(drivers[index + numRows]) + "<td></td>"
                        + getDriverInputHtml(drivers[index + 2*numRows])
                        + "</tr>";
  }
    
  var tableHtml = "<table border='1' style='font-size: 18px;'><tbody>";
  tableHtml += "<tr><th>Check</th><th>Driver</th><th>Value</th><th></th>";
  tableHtml += "<th>Check</th><th>Driver</th><th>Value</th><th></th>";
  tableHtml += "<th>Check</th><th>Driver</th><th>Value</th><th></th>";
  tableHtml += "<th>Check</th><th>Driver</th><th>Value</th></tr>";
  tableHtml += tableBody;
  tableHtml += "</tbody></table>";
  var driverTable = document.getElementById('driverTable');
  driverTable.innerHTML = tableHtml;
    
  // Request Updated Lineup.
  requestUpdatedLineup();
}

function getDrivers(responseJSON) {
  var drivers = [];
  var driverObjects = JSON.parse(responseJSON);
  for (var index in driverObjects) {
    var driverObject = driverObjects[index];
    var name = driverObject.name;
    var rank = driverObject.rank;
    var score = driverObject.score;
    var driver = new Driver(name, rank, score);
    drivers.push(driver);
  }
  return drivers;
}

function requestUpdatedLineup() {
  var teamSelect = document.getElementById("teamselect");
  var raceSelect = document.getElementById("raceselect");
  var params = 'item=lineup&teamname=' + teamSelect.value + '&raceid=' + raceSelect.value;
  sendMessage('/admin/api', params, selectDrivers);
}

function getDriverInputHtml(driver) {
  var name = driver.name;
  var value = driver.value;
  var inputHtml= "<td><input id='" + name + "'type='checkbox' name='driver' value='" +
      name + "' onclick='updateSelection(this, \"" + name + "\", " + value + ")'></td>";
  inputHtml += "<td>" + name + "</td>";
  inputHtml += "<td>" + value + "</td>";
  return inputHtml;
}

function selectDrivers(responseJSON)
{
  console.log(responseJSON);
  clearSelections();
  var selectedDriverObjects = JSON.parse(responseJSON);
  for (var index in selectedDriverObjects) {
    var selectedDriverObject = selectedDriverObjects[index];
    var name = selectedDriverObject.name;
    var rank = selectedDriverObject.rank;
    var score = selectedDriverObject.score;
    var driver = new Driver(name, rank, score);
    selectDriver(driver);
  }
  validatePicks();
}

function clearSelections() {
  for (var driverName in selectedDrivers) {
    var input = document.getElementById(driverName);
    if (input) {
      input.checked = false;
    }
  }
  selectedDrivers = {};
  points = 0;
  document.getElementById('currentDrivers').innerHTML = "";
  document.getElementById('totalValue').innerHTML = points;
}

function selectDriver(driver) {
  var name = driver.name;
  var input = document.getElementById(name);
  input.checked = true;
  var value = driver.value;
  updateSelection(input, name, value);
}

function updateSelection(input, name, value) {
  if (input.checked) {
    selectedDrivers[name] = value;
    points += value;
  } else if (selectedDrivers[name]) {
    delete selectedDrivers[name];
    points -= value;
  }
  var html = "";
  for (var driver in selectedDrivers) {
    html += driver + " - " + selectedDrivers[driver] + "<BR/>";
  }
  document.getElementById('currentDrivers').innerHTML = html;
  document.getElementById('totalValue').innerHTML = points;
  validatePicks();
}

function validatePicks() {
  var submit = document.getElementById('submit');
  var numKeys = getNumKeys(selectedDrivers);
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