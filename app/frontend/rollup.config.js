import {nodeResolve} from '@rollup/plugin-node-resolve';
import typescript from '@rollup/plugin-typescript';
import {terser} from "rollup-plugin-terser";

const plugins =  [
  nodeResolve(),
  terser({
    mangle: {
      properties:true,
    },
  }),
  typescript(),
]

export default [
  {
    input: 'src/scripts/index.ts',
    output: {
      file: 'build/scripts/index.js',
      format: 'iife',
    },
    plugins: plugins
  },
  {
    input: 'src/scripts/logout.ts',
    output: {
      file: 'build/scripts/logout.js',
      format: 'iife',
    },
    plugins: plugins
  }
];
