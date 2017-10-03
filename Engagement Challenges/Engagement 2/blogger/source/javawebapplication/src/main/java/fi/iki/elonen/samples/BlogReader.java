package fi.iki.elonen.samples;

/*
 * #%L
 * NanoHttpd-JavaWebApplication
 * %%
 * Copyright (C) 2012 - 2015 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import fi.iki.elonen.HTTP.Div;
import fi.iki.elonen.HTTP.HTTPUnit;
import fi.iki.elonen.HTTP.MapTokenResolver;

import java.util.ArrayList;

/**
 *
 */
public class BlogReader extends HTTPUnit {
    public static ArrayList<Blog> blogs = new ArrayList<>(10);
    static {
        blogs.add(new Blog(
                "2015 Food Media South",
                "Mary Beth Lasseter",
                "Food Media South, a Southern Foodways Alliance symposium, set for February 27-28 in Birmingham, Alabama, will explore storytelling in the digital era.\n" +
                        "The SFA, an institute of the Center for the Study of Southern Culture at the University of Mississippi, has documented, studied, and celebrated the diverse food cultures of the changing American South since 1999. Over the last five years, the SFA has emerged as a leading content creator, the publisher of a quarterly journal, a book series, a mobile app, a biweekly podcast, and dozens of films. With Food Media South, the SFA invests in the future of food-focused content creation and foodways storytelling.\n" +
                        "\n" +
                        "The rise of digital media has created new publication opportunities. And new challenges. Web and mobile platforms debut each week. And old platforms, like journals and zines, are dynamic again. Podcasts, once the sleepy stepchild of radio, are celebrated as the most dynamic audio distribution medium. The field of food journalism is exploding. SFA aims to help you find your footing.\n" +
                        "\n" +
                        "If you blog about food and want to thread more narrative into your work, this conference is for you. If you produce a podcast and want to build your personal brand, this one-and-one-half-day intensive is what you need. If you edit a digital magazine and want to move from short reportage to long form journalism, head this way. If you write short newspaper blurbs and want to graduate to discursive magazine articles, you’re in the right place.\n" +
                        "\n" +
                        "Speakers include Dorothy Kalins, founding editor of Saveur magazine, chair of the James Beard Journalism Awards committee, and producer of beautiful books; Chuck Reece, editor-in-chief of Bitter Southerner; Erika Council, proprietor of the blog Southern Souffle; Roscoe Hall II, proprietor of the site Punk as Food; and Bill Addison, restaurant editor of Eater National.\n" +
                        "\n" +
                        "The day will begin with concurrent sessions before branching into afternoon tracks. Assignments to those tracks will be made in January.\n" +
                        "\n" +
                        "Birmingham, Alabama, the media hub where fifty founders of the SFA first gathered in 1999 to forge the organization, will serve as the home of Food Media South. Funding from the Greater Birmingham Convention and Visitors Bureau and the Alabama Tourism Department underwrites the event.\n" +
                        "\n" +
                        "Rosewood Hall, in walkable downtown Homewood, will be our headquarters for the weekend. A Friday night cocktail party, with bites from chef Frank Stitt of Highlands Bar and Grill in Birmingham, will kick off the symposium. On Saturday, participants will wake to breakfast from Little Donkey and enjoy a meat-and-three lunch showcase from Eagles Soul Food and Johnny’s. A Saturday night dinner—One Pig, Two Pies, One Pint—staged at Good People Brewing, alongside Railroad Park, will close the weekend.\n" +
                        "\n" +
                        "In 2010, Shaun Chavis and Jason Horn staged the first Food Blog South, a groundbreaking food media event in Birmingham, Alabama. For four subsequent years, they catalyzed important conversations about media ethics, technology potential, and creative enterprise. The Southern Foodways Alliance now serves as the host of a re-branded the event as Food Media South. SFA thanks Shaun and Jason for their leadership."
        ));
        blogs.add(new Blog(
                "Amateur radio in the news: class a huge success, NPOTA, tower woes",
                "Dan JB6NU",
                "Amateur radio class a huge success. This year’s Amateur Radio Technician class was a huge success. This class started out with nine inquiring applicants and settled with a group of  five very interested and focused individuals.\n" +
                        "\n" +
                        "Aksarben ARC coming to Homestead. In coordination with the National Park Service Centennial, members of the Aksarben Amateur Radio Club will be communicating around the nation, and possibly outside of the United States, at Homestead National Monument of America on Saturday, Jan. 23, 2016 from 9 a.m. to 4:30 p.m. They will be based out of the Education Center. This is part of the National Parks on the Air program sponsored by the American Radio Relay League. Radio operators throughout the nation will be communicating with each other and spreading information about national parks through 2016 and to show people the capabilities of amateur radio and how it can benefit the general public. “The Centennial of the National Park Service is here and during this year there will be opportunities to engage in unique activities,” Superintendent Mark Engler said in a press release. “We are very excited for the ham radio operators from the area to use the park all year long to participate in this program.”\n" +
                        "\n" +
                        "Ham radio operator asked to take down tower. A federally licensed amateur radio operator said he plans to sue the city for harassment in regards to a radio tower he was forced to remove. Adams Street resident Karl Reed said the city violated his First Amendment rights and failed to give him due process when it threatened to fine his landlord $2,500 a day if the tower was not removed."
        ));
    }

    @Override
    public String toString() {
        StringBuilder theblogs = new StringBuilder(100);
        int i = 1;
        for (Blog blog : blogs) {
            MapTokenResolver tr = new MapTokenResolver();
            tr.put("author", blog.getAuthor());
            tr.put("title", blog.getTitle());
            tr.put("content", blog.getContent());
            theblogs.append(
                    Div.fromStatic("blog-" + i, "templates/blog.html", tr)
            );
        }
        return theblogs.toString();
    }
}
