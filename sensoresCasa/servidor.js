var http = require("http");
var url = require("url");
var fs = require("fs");
var path = require("path");
var socketio = require("socket.io");
var MongoClient = require('mongodb').MongoClient;
var MongoServer = require('mongodb').Server;
var mimeTypes = { "html": "text/html", "jpeg": "image/jpeg", "jpg": "image/jpeg", "png": "image/png", "js": "text/javascript", "css": "text/css", "swf": "application/x-shockwave-flash"};

function comprobarTemp(valor) {
    var temp = parseInt(valor);

	if (valor>30) 
        return 1;   //"La temperatura es muy alta";
	else if (valor<15) 
        return 2;   //"La temperatura es muy baja";
	else 
        return 0;
}

function comprobarLum(valor) {
    var lum = parseInt(valor);
    
	if (valor>20) 
        return 1;   //"La temperatura es muy alta";
	else if (valor<5) 
        return 2;   //"La temperatura es muy baja";
	else 
        return 0;
}


var httpServer = http.createServer(
	function(request, response) {
		var uri = url.parse(request.url).pathname;
        if(request.url==='/favicon.ico'){
            response.writeHead(200,{'Content-Type':'image/x-icon'});
            response.end();
            return;
        }
		if (uri=="/") uri = "/cliente.html";
        if (uri=="/sensores") uri = "/sensores.html";
  		if (uri=="/control") uri = "/cliente.html";
		var fname = path.join(process.cwd(), uri);
		fs.exists(fname, function(exists) {
			if (exists) {
				fs.readFile(fname, function(err, data){
					if (!err) {
						var extension = path.extname(fname).split(".")[1];
						var mimeType = mimeTypes[extension];
						response.writeHead(200, mimeType);
						response.write(data);
						response.end();
					}
					else {
						response.writeHead(200, {"Content-Type": "text/plain"});
						response.write('Error de lectura en el fichero: '+uri);
						response.end();
					}
				});
			}
			else{
				console.log("Peticion invalida: "+uri);
				response.writeHead(200, {"Content-Type": "text/plain"});
				response.write('404 Not Found\n');
				response.end();
			}
		});
	}
);


var mongoClient = new MongoClient(new MongoServer('localhost', 27017));
mongoClient.connect("mongodb://localhost:27017/TempLumDB", function(err, db) {
	httpServer.listen(8080);
	var io = socketio.listen(httpServer);
	
	db.createCollection("Datos", function(err, collection){
    	io.sockets.on('connection',function(client) {
            
            console.log('Usuario Conectado');
            
            //Esperando cambios en las medidas de los sensores
            client.on('cambioSensores', function (data) { 
                //Inserto los valores recibidos en la base de daros
                collection.insert(data, {safe:true}, function(err, result) {});
                //Envio los valores a los clientes
                var val1 = data.temp;   console.log(val1);
                var val2 = data.lum;	console.log(val2);
		var val3 = data.aire;	console.log(val3);
		var val4 = data.persiana; console.log(val4);
                
               
                var alertaTemp = "";
                var alertaLumm = "";

                
                var alertaTmp = comprobarTemp(val1);
                if(alertaTmp==1)
                    var alertaTemp = "La temperatura es muy alta";
                else if(alertaTmp==2)
                    var alertaTemp = "La temperatura es muy baja";
                
                var alertaLum = comprobarLum(val2);
                if(alertaLum==1)
                    var alertaLumm = "La luminosidad es muy alta";
                else if(alertaLum==2)
                    var alertaLumm = "La luminosidad es muy baja";
                
		//condiciones para cambiar actuadores y mostrar alertas
                if(alertaTmp==1 && alertaLum==1){ //bajar persiana temperatura alta y luminosidad alta
                    io.emit('alertas', {alerta1: alertaTemp, alerta2: alertaLumm, persiana: 'Bajadas', aire: val3});
                }else if(alertaTmp==2 && alertaLum==2){	//subir persiana por temperatura baja y luminosidad baja
		    io.emit('alertas', {alerta1: alertaTemp, alerta2: alertaLumm, persiana: 'Subidas', aire: val3});
		}else if(alertaTmp==1 && val4=='Subidas'){//bajar persiana si temperatura alta y estaba subida
			console.log('entra');
		    io.emit('alertas', {alerta1: alertaTemp, alerta2: alertaLumm, persiana: 'Bajadas', aire: val3});
		}else if(alertaTmp==1 && val3=='OFF'){//encender aire si la temperatura esta alta y esta apagado
		    io.emit('alertas', {alerta1: alertaTemp, alerta2: alertaLumm, persiana: val4, aire: 'ON'});
		    io.emit('actualizarAireCasa', 'ON');
		}else if(alertaTmp==2 && val3=='ON' ){//apagar aire si la temperatura esta baja y esta encendido
		    io.emit('alertas', {alerta1: alertaTemp, alerta2: alertaLumm, persiana: val4, aire: 'OFF'});
		    io.emit('actualizarAireCasa', 'OFF');
		}else if(alertaLum==2 && val4=='Bajadas'){ // subir persianas si la luminosidad es baja y están bajadas
		    io.emit('alertas', {alerta1: alertaTemp, alerta2: alertaLumm, persiana: 'Subidas', aire: val3});
		}else{
                    if(alertaTmp!=0){   //alerta temperatura
                        io.emit('alertaT', alertaTemp);
                    }else if(alertaLum!=0){     //alerta luminosidad
                        io.emit('alertaL', alertaLumm);
                    }        
                }
                io.emit('actualizarDatosCliente', {temp: val1, lum: val2, alertaT:alertaTemp, alertaL:alertaLumm});
                
         
			});
            
            //Esperando cambios en la posición del aire acondicionado
            client.on('cambioAire', function (data) { //console.log('cambioAire');
                //Inserto los valores recibidos en la base de daros
                //collection.insert(data, {safe:true}, function(err, result) {});
                
                //Envio los valores a la casa
                var val1 = data.posAire;   //console.log(val1);
                io.emit('actualizarAireCasa', val1);
			});
            
            //Esperando cambios en la posición del aire acondicionado
            client.on('cambioPersianas', function (data) { //console.log('cambioPersianas');
                //Inserto los valores recibidos en la base de daros
                //collection.insert(data, {safe:true}, function(err, result) {});
                
                //Envio los valores a la casa
                var val1 = data.posPersianas;   //console.log(val1);
                io.emit('actualizarPersianasCasa', val1);
			});
            
		});
    });
});

