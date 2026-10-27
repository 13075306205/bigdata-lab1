const path = require("path");
const { chromium } = require("playwright");

const outputDir = path.join(__dirname, "figures");
const targets = [
    { url: "http://localhost:9870/explorer.html#/lab2/input", file: "figure-1-hdfs-input.png" },
    { url: "http://localhost:8088/cluster/apps/FINISHED", file: "figure-2-yarn-status.png" },
    { url: "http://localhost:9870/explorer.html#/lab2/output", file: "figure-3-hdfs-output.png" },
];

(async () => {
    const browser = await chromium.launch({ headless: true, channel: "chrome" });
    const page = await browser.newPage({
        viewport: { width: 1280, height: 760 },
        deviceScaleFactor: 2,
    });

    for (const target of targets) {
        await page.goto(target.url, { waitUntil: "networkidle", timeout: 20000 });
        await page.waitForTimeout(1200);
        await page.screenshot({ path: path.join(outputDir, target.file), fullPage: false });
        console.log(`Saved ${target.file}`);
    }

    await browser.close();
})().catch((error) => {
    console.error(error);
    process.exit(1);
});
