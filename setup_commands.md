# Setup Commands — Run in order

## Step 1: Clone Fabric template
git clone https://github.com/FabricMC/fabric-example-mod flowinventory
cd flowinventory

## Step 2: Open in IntelliJ
# File → Open → select the flowinventory folder
# Wait for Gradle sync (5-10 minutes first time)

## Step 3: Verify Gradle works
./gradlew genSources

## Step 4: Run Minecraft with the mod
./gradlew runClient
