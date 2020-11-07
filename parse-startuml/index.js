'use strict';

const fs = require('fs');
const path = require('path');

const rawdata = fs.readFileSync(path.join(__dirname, '../temp-sample.mdj'));
const starUmlData = JSON.parse(rawdata);

function traverse(o, func) {
	//function process(key, value, obj)
	for (var i in o) {
		func.apply(this, [i, o[i], o]);
		if (o[i] !== null && typeof(o[i]) == "object") {
			//going one step down in the object tree!!
			traverse(o[i], func);
		}
	}
}

let umlObjects = [];
{
	traverse(starUmlData, (k, v, o) => {
		if (k === '_type' && v === 'UMLObject') {
			umlObjects.push(o);
		}
	});
	umlObjects = umlObjects.reduce((prev, curr) => {
		prev[curr._id] = curr;
		return prev;
	}, {});
}

let umlInterfaces = [];
{
	traverse(starUmlData, (k, v, o) => {
		if (k === '_type' && v === 'UMLInterface') {
			umlInterfaces.push(o);
		}
	});
	umlInterfaces = umlInterfaces.reduce((prev, curr) => {
		prev[curr._id] = curr;
		return prev;
	}, {});
}

let umlLinkViews=[];
{
	
	traverse(starUmlData, (k, v, o) => {
		if (k === '_type' && v === 'UMLLinkView') {
			umlLinkViews.push(o);
		}
	});

	umlLinkViews = umlLinkViews.reduce((prev, curr) => {
		prev[curr._id] = curr;
		return prev;
	}, {});
}

const data = starUmlData.ownedElements
	.map(e => e.ownedElements)
	//.filter(e=>e._type==='UMLModel')
	.reduce((prev, curr) => prev.concat(curr), [])
	.filter(e => e._type === 'UMLClassDiagram' && e.name === 'Rest Endpoints overview')
	.reduce((prev, curr) => prev.concat(curr.ownedViews), [])
	.filter(e=>e._type==='UMLObjectView')
	.map(e => {
		const name = umlObjects[e.model.$ref].name;
		let method = (umlObjects[e.model.$ref].classifier||{$ref:undefined}).$ref;
		method=(umlInterfaces[method]||{name:undefined}).name;
		const path = getPath(e);
		const path3 = e;
		return {
			name,
			method,
			path,
			path3
		};
	});
function getPath(e){
	const umlLinkView= Object.values(umlLinkViews).filter(umlLinkView => {
		return umlLinkView.head.$ref === e._id || umlLinkView.tail.$ref === e._id;
	})[0];
	
	let parentEnd;
	if(umlLinkView.head.$ref===e._id){
		parentEnd=umlLinkView.tail.$ref
	}else if(umlLinkView.tail.$ref===e._id){
		parentEnd=umlLinkView.head.$ref
	}else{
		throw 'Error handling head/tail'
	}

	return parentEnd;
}
//that's all... no magic, no bloated framework

console.log(data);