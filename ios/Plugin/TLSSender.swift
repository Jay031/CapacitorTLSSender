import Foundation
import Capacitor
import Network

@objc(TLSSender)
public class TLSSender: CAPPlugin {

    private var connection: NWConnection?

    @objc func makeTLSTCPRequest(_ call: CAPPluginCall) {
        guard let host = call.getString("host"),
              let port = call.getInt("port"),
              let message = call.getString("message") else {
            call.reject("Invalid host, port, or message")
            return
        }

        let parameters = NWParameters.tls
        parameters.defaultProtocolStack.applicationProtocols.insert("http/1.1", at: 0)

        let queue = DispatchQueue(label: "tcp_tls_queue")
        connection = NWConnection(host: NWEndpoint.Host(host), port: NWEndpoint.Port(rawValue: UInt16(port))!, using: parameters)
        connection?.stateUpdateHandler = { [weak self] state in
            switch state {
            case .ready:
                self?.sendData(message)
            case .failed(let error):
                self?.call.reject("Failed to make TLS request: \(error.localizedDescription)")
                self?.connection = nil
            default:
                break
            }
        }

        connection?.start(queue: queue)
    }

    private func sendData(_ message: String) {
        guard let connection = connection else {
            call.reject("Connection is not available")
            return
        }

        let content = message.data(using: .utf8)
        let completion = NWConnection.SendCompletion.contentProcessed { [weak self] error in
            if let error = error {
                self?.call.reject("Failed to send data: \(error.localizedDescription)")
            } else {
                self?.receiveData()
            }
        }

        connection.send(content: content, completion: completion)
    }

    private func receiveData() {
        guard let connection = connection else {
            call.reject("Connection is not available")
            return
        }

        connection.receive(minimumIncompleteLength: 1, maximumLength: 65536) { [weak self] (data, context, isComplete, error) in
            if let data = data,
               let responseData = String(data: data, encoding: .utf8) {
                let result = ["response": responseData]
                self?.call.resolve(result)
            } else if let error = error {
                self?.call.reject("Failed to receive data: \(error.localizedDescription)")
            }

            connection.cancel()
            self?.connection = nil
        }
    }
}
