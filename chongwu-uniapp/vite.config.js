import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { copyFileSync, existsSync, mkdirSync, readdirSync, statSync } from 'node:fs'
import { resolve, join } from 'node:path'

function copyDir(src, dest) {
	if (!existsSync(src)) return
	mkdirSync(dest, { recursive: true })
	for (const entry of readdirSync(src)) {
		const srcPath = join(src, entry)
		const destPath = join(dest, entry)
		const stat = statSync(srcPath)
		if (stat.isDirectory()) {
			copyDir(srcPath, destPath)
		} else {
			mkdirSync(resolve(destPath, '..'), { recursive: true })
			copyFileSync(srcPath, destPath)
		}
	}
}

function copyStaticForH5() {
	return {
		name: 'copy-static-for-h5',
		closeBundle() {
			const src = resolve(__dirname, 'static')
			const dest = resolve(__dirname, 'unpackage/dist/build/web/static')
			copyDir(src, dest)
		}
	}
}

export default defineConfig({
	plugins: [
		uni(),
		copyStaticForH5()
	]
})
