```plantuml
[Web Frontend] as web
[Core] as core
[EventBus] as bus
interface "Rest Api" as api
[Bot] as bot

web --> api
bot -> bus
api --> core
core --> bus
```