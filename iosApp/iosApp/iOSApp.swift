import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    
    init(){
        AppConfigInitializerKt.doInitAppConfig()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
