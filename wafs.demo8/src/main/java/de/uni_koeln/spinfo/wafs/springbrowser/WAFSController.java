package de.uni_koeln.spinfo.wafs.springbrowser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import de.uni_koeln.spinfo.wafs.trackdb.TrackDB;
import de.uni_koeln.wafs.datakeeper.query.Result;
import de.uni_koeln.wafs.datakeeper.query.WAFSQuery;

@Controller
public class WAFSController {

	private Logger logger = Logger.getLogger(getClass());
	
	/*
	 * This variable will be automatically set during Spring Startup.
	 * It is not even required to implement getter/setter-metods, although
	 * the variable is private. 
	 */
	@Autowired
	private WAFSBackend backend;
	
	private void setPageTitle(ModelAndView mv, String title) {
		mv.addObject("pageTitle", title);
	}
	
	@RequestMapping("/stream")
	public void getPreview(@RequestParam("path") String location, HttpServletResponse response) throws URISyntaxException, IOException {
		logger.info("Requesting stream: " + location);
		 URI uri = new URI(location);
		  File mp3 = new File(uri);
		  response.setContentType("audio/mpeg"); 
		  response.addHeader("Content-Disposition", "attachment; filename=" + uri);
		  response.setContentLength((int) mp3.length());
		  FileInputStream input = new FileInputStream(mp3);
		  BufferedInputStream buf = new BufferedInputStream(input);
		  int readBytes = 0;
		  logger.info("Sending stream to response.getOutputStream()");
		  ServletOutputStream out = response.getOutputStream();
		  while ((readBytes = buf.read()) != -1) {
		    out.write(readBytes);
		  }
		  buf.close();
	}
		
	@RequestMapping("/search")
	public ModelAndView quickSearch(WAFSQuery query) throws IOException {
		logger.info("Searching for " + query);
		ModelAndView mv = new ModelAndView("search3");
		mv.addObject("wafsQuery", query);
		query.setPageSize(20);
		if(query.getValues() != null && query.getValues().size() > 0) {
			Result result = backend.getTrackDB().search(query);
			logger.info("Result: " + result.getMaxEntries() + " entries.");
			mv.addObject(result);
		} else {
			mv.addObject(new Result());
		}
		return mv;
	}
	
	@RequestMapping("/demo")
	public ModelAndView demo() {
		return new ModelAndView("browse_albums");
	}
	
	
	/**
	 * localhost:8080/artists.html
	 * 
	 * localhost:8080/artist/D.html
	 * 
	 * localhost:8080/artist/S/2.html
	 */
	
	@RequestMapping("artists")
	public ModelAndView artists() throws IOException {
		return artists("A");
	}
	
	@RequestMapping("artists/{letter}")
	public ModelAndView artists(@PathVariable("letter") String letter) throws IOException {
		return artists(letter,0);
	}

	@RequestMapping("artists/{letter}/{pageNumber}")
	private ModelAndView artists(@PathVariable("letter") String letter, @PathVariable("pageNumber") int pageNumber) throws IOException{
		TrackDB trackDB = backend.getTrackDB();
		Result result = trackDB.searchDistinct("artist_exact", letter + "*", pageNumber, 10);
		System.out.println("Results: " + result.getMaxEntries());
		ModelAndView mv = new ModelAndView("browse_artists");
		mv.addObject(result);
		return mv;
	}
	
}
