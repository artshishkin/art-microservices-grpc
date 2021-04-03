const grpc = require('grpc')
const protoLoader = require('@grpc/proto-loader')

const packageDef = protoLoader.loadSync('proto/bank-service.proto')
const protoDescriptor = grpc.loadPackageDefinition(packageDef)

const client = new protoDescriptor.BankService('localhost:6565', grpc.credentials.createInsecure())

client.getBalance({accountNumber: 4}, (err, balance) => {
    if (err) {
        console.error('something bad happened' + err)
    } else {
        console.log('Received balance: ' + balance.amount)
    }

})